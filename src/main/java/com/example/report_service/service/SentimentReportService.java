package com.example.report_service.service;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.example.global.exception.type.NotFoundException;
import com.example.report_service.client.service.SurveyClientService;
import com.example.report_service.dto.internal.OverallStats;
import com.example.report_service.dto.internal.SentimentStats;
import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.request.QuestionAnswerRequest;
import com.example.report_service.dto.response.*;
import com.example.report_service.entity.OverallSentimentReport;
import com.example.report_service.entity.SentimentReport;
import com.example.report_service.exception.ReportExceptionType;
import com.example.report_service.repository.OverallSentimentReportRepository;
import com.example.report_service.repository.SentimentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentimentReportService {

    private final SentimentReportRepository sentimentReportRepository;
    private final OverallSentimentReportRepository overallReportRepository;
    private final AwsComprehendService awsComprehendService;
    private final SurveyClientService surveyClientService;

    @Transactional
    public void aggregateAndGenerateReport(AggregateRequest aggregateRequest) {
        Long surveyId = aggregateRequest.surveyId();
        Long responseId = aggregateRequest.responseId();
        Long userId = aggregateRequest.userId();

        Set<Long> processedQuestionIds = processAggregateRequest(aggregateRequest, surveyId, responseId, userId);

        processedQuestionIds.forEach(questionId -> generateOverallReportForQuestion(surveyId, questionId));
    }

    /**
     * AggregateRequest 내의 모든 QuestionAnswerRequest를 처리하여 개별 감성 보고서를 생성하고,
     * 처리한 질문 ID 집합을 반환합니다.
     */
    private Set<Long> processAggregateRequest(AggregateRequest aggregateRequest, Long surveyId, Long responseId, Long userId) {
        Set<Long> processedQuestionIds = new HashSet<>();

        aggregateRequest.answers().forEach(answer -> {
            validateAnswerText(answer);
            processAnswer(surveyId, responseId, userId, answer);
            processedQuestionIds.add(answer.questionId());
        });

        return processedQuestionIds;
    }

    private void validateAnswerText(QuestionAnswerRequest answer) {
        String text = answer.text();
        if (text == null || text.trim().isEmpty()) {
            throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
        }
    }

    /**
     * 하나의 QuestionAnswerRequest에 대해 텍스트 감성 분석을 수행하고 개별 감성 보고서를 저장합니다.
     */
    private void processAnswer(Long surveyId, Long responseId, Long userId, QuestionAnswerRequest answer) {
        String text = answer.text();

        // 텍스트 감성 분석 수행
        SentimentStats stats = analyzeText(text);
        if (stats.getTotal() == 0) {
            throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
        }

        // 평균 값 계산
        double avgPositive = stats.getAvgPositive();
        double avgNegative = stats.getAvgNegative();
        double avgNeutral  = stats.getAvgNeutral();
        double avgMixed    = stats.getAvgMixed();

        // 개별 감성 보고서 엔티티 생성 및 저장
        SentimentReport report = SentimentReport.builder()
                                                .surveyId(surveyId)
                                                .questionId(answer.questionId())
                                                .responseId(responseId)
                                                .userId(userId)
                                                .totalResponses(stats.getTotal())
                                                .positiveCount(stats.getPositiveCount())
                                                .negativeCount(stats.getNegativeCount())
                                                .neutralCount(stats.getNeutralCount())
                                                .mixedCount(stats.getMixedCount())
                                                .averagePositive(avgPositive)
                                                .averageNegative(avgNegative)
                                                .averageNeutral(avgNeutral)
                                                .averageMixed(avgMixed)
                                                .build();

        sentimentReportRepository.save(report);
    }

    /**
     * 특정 설문 및 질문에 대해 개별 보고서들을 조회한 후, 전체 통계 보고서를 생성하거나 업데이트합니다.
     */
    private void generateOverallReportForQuestion(Long surveyId, Long questionId) {
        List<SentimentReport> reports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId);
        if (reports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND);
        }

        // 개별 보고서들을 기반으로 전체 통계(가중 평균 등)를 계산
        OverallStats stats = OverallStats.fromReports(reports);

        OverallSentimentReport overallReport;
        if (overallReportRepository.existsBySurveyIdAndQuestionId(surveyId, questionId)) {
            overallReport = overallReportRepository.findBySurveyIdAndQuestionId(surveyId, questionId)
                                                   .orElseThrow(() -> new NotFoundException(ReportExceptionType.OVERALL_REPORT_NOT_FOUND));
            overallReport.updateStats(
                    stats.getTotalResponses(),
                    stats.getPositiveCount(),
                    stats.getNegativeCount(),
                    stats.getNeutralCount(),
                    stats.getMixedCount(),
                    stats.getOverallPositive(),
                    stats.getOverallNegative(),
                    stats.getOverallNeutral(),
                    stats.getOverallMixed()
            );
            overallReport = overallReportRepository.save(overallReport);
        } else {
            overallReport = OverallSentimentReport.builder()
                                                  .surveyId(surveyId)
                                                  .questionId(questionId)
                                                  .totalResponses(stats.getTotalResponses())
                                                  .positiveCount(stats.getPositiveCount())
                                                  .negativeCount(stats.getNegativeCount())
                                                  .neutralCount(stats.getNeutralCount())
                                                  .mixedCount(stats.getMixedCount())
                                                  .averagePositive(stats.getOverallPositive())
                                                  .averageNegative(stats.getOverallNegative())
                                                  .averageNeutral(stats.getOverallNeutral())
                                                  .averageMixed(stats.getOverallMixed())
                                                  .build();
            overallReport = overallReportRepository.save(overallReport);
        }

        // 개별 보고서와의 연관관계 동기화
        for (SentimentReport child : reports) {
            child.addOverallSentimentReportAndSentimentReport(overallReport);
        }
        sentimentReportRepository.saveAll(reports);
    }

    /**
     * 텍스트를 기반으로 AWS Comprehend 또는 유사한 서비스를 호출하여 감성 분석 결과를 누적한 통계 데이터를 반환합니다.
     */
    private SentimentStats analyzeText(String text) {
        SentimentStats stats = new SentimentStats();
        if (text != null && !text.trim().isEmpty()) {
            DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
            AWSComprehendResult result = AWSComprehendResult.from(dsr);
            stats.accumulate(result);
        }
        return stats;
    }

    public SentimentReportDto  getAllSentimentReportBySurveyAndQuestion(Long surveyId, Long questionId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SentimentReport> sentimentReports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId, pageable);

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(surveyId, questionId);

        if (sentimentReports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        Page<SentimentReportDetailDto> reportItems = sentimentReports.map(SentimentReportDetailDto::from);
        return new SentimentReportDto(questionWithSurveyDto, reportItems);
    }

    public SentimentReportSingleDto getSentimentReport(Long sentimentId) {
        SentimentReport sentimentReport = sentimentReportRepository.findById(sentimentId)
                                                                   .orElseThrow(() -> new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND));

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(sentimentReport.getSurveyId(), sentimentReport.getQuestionId());

        return SentimentReportSingleDto.from(sentimentReport, questionWithSurveyDto);
    }

    public Page<OverallSentimentReportSummaryDto> getAllOverallReportBySurvey(Long surveyId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OverallSentimentReport> overallSentimentReports = overallReportRepository.findBySurveyId(surveyId, pageable);

        if (overallSentimentReports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        return overallSentimentReports.map(entity -> {
            QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(
                    entity.getSurveyId(), entity.getQuestionId()
            );

            return OverallSentimentReportSummaryDto.from(
                    entity.getId(),
                    questionWithSurveyDto.title(),
                    questionWithSurveyDto.questionId(),
                    questionWithSurveyDto.questionText()
            );
        });
    }

    public OverallSentimentReportDto getOverallReportById(Long overallReportId) {
        OverallSentimentReport overallSentimentReport = overallReportRepository.findById(overallReportId)
                                                                               .orElseThrow(() -> new NotFoundException(ReportExceptionType.OVERALL_REPORT_NOT_FOUND));

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(overallSentimentReport.getSurveyId(), overallSentimentReport.getQuestionId());

        return OverallSentimentReportDto.from(overallSentimentReport, questionWithSurveyDto.title(), questionWithSurveyDto.questionText());
    }

}