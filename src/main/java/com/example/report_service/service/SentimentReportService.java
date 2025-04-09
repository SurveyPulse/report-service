package com.example.report_service.service;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.example.global.exception.type.NotFoundException;
import com.example.report_service.client.service.SurveyClientService;
import com.example.report_service.dto.internal.OverallStats;
import com.example.report_service.dto.internal.SentimentStats;
import com.example.report_service.dto.request.AggregateRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentimentReportService {

    private final SentimentReportRepository sentimentReportRepository;
    private final OverallSentimentReportRepository overallReportRepository;
    private final AwsComprehendService awsComprehendService;
    private final SurveyClientService surveyClientService;

    @Transactional
    public void aggregateReport(AggregateRequest aggregateRequest) {

        Long surveyId = aggregateRequest.surveyId();
        Long responseId = aggregateRequest.responseId() != null ? aggregateRequest.responseId() : 0L;

        // 여러 질문에 대한 답변을 순회
        aggregateRequest.answers().forEach(answer -> {
            String text = answer.text();
            if (text == null || text.trim().isEmpty()) {
                throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
            }

            // 텍스트 감성 분석
            SentimentStats stats = analyzeText(text);
            if (stats.getTotal() == 0) {
                throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
            }

            // 평균 계산
            double avgPositive = stats.getAvgPositive();
            double avgNegative = stats.getAvgNegative();
            double avgNeutral  = stats.getAvgNeutral();
            double avgMixed    = stats.getAvgMixed();

            SentimentReport report = SentimentReport.builder()
                                                    .surveyId(surveyId)
                                                    .questionId(answer.questionId())
                                                    .responseId(responseId)
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
        });
    }

    private SentimentStats analyzeText(String text) {
        SentimentStats stats = new SentimentStats();
        if (text != null && !text.trim().isEmpty()) {
            DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
            AWSComprehendResult result = AWSComprehendResult.from(dsr);
            stats.accumulate(result);
        }
        return stats;
    }

    @Transactional
    public void generateOverallReport(Long surveyId, Long questionId) {
        List<SentimentReport> reports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId);
        if (reports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND);
        }

        // 가중 평균 계산
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

    public Page<SentimentReportDto> getSentimentReportsBySurveyAndQuestion(Long surveyId, Long questionId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SentimentReport> sentimentReports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId, pageable);

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(surveyId, questionId);

        if (sentimentReports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        return sentimentReports.map(entity -> SentimentReportDto.from(entity, questionWithSurveyDto));
    }

    public SentimentReportDto getSentimentReport(Long sentimentId) {
        SentimentReport sentimentReport = sentimentReportRepository.findById(sentimentId)
                                                                   .orElseThrow(() -> new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND));

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(sentimentReport.getSurveyId(), sentimentReport.getQuestionId());

        return SentimentReportDto.from(sentimentReport, questionWithSurveyDto);
    }

    public OverallSentimentReportDto getOverallReport(Long surveyId, Long questionId) {
        OverallSentimentReport overallSentimentReport = overallReportRepository.findBySurveyIdAndQuestionId(surveyId, questionId)
                                                                               .orElseThrow(() -> new NotFoundException(ReportExceptionType.OVERALL_REPORT_NOT_FOUND));
        return OverallSentimentReportDto.from(overallSentimentReport);
    }

}
