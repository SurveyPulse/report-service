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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        log.info("감성 보고서 집계 시작 - 설문 ID: {}, 응답 ID: {}, 사용자 ID: {}", surveyId, responseId, userId);

        Set<Long> processedQuestionIds = processAggregateRequest(aggregateRequest, surveyId, responseId, userId);
        log.info("처리된 질문 ID 집합: {}", processedQuestionIds);

        processedQuestionIds.forEach(questionId -> {
            log.info("설문 ID [{}], 질문 ID [{}]에 대해 전체 감성 보고서 생성/갱신 시작", surveyId, questionId);
            generateOverallReportForQuestion(surveyId, questionId);
            log.info("설문 ID [{}], 질문 ID [{}]에 대해 전체 감성 보고서 생성/갱신 완료", surveyId, questionId);
        });
    }

    /**
     * AggregateRequest 내의 모든 QuestionAnswerRequest를 처리하여 개별 감성 보고서를 생성하고,
     * 처리한 질문 ID 집합을 반환합니다.
     */
    private Set<Long> processAggregateRequest(AggregateRequest aggregateRequest, Long surveyId, Long responseId, Long userId) {
        Set<Long> processedQuestionIds = new HashSet<>();

        aggregateRequest.answers().forEach(answer -> {
            log.debug("질문 ID [{}]에 대한 답변 처리 시작", answer.questionId());
            validateAnswerText(answer);
            processAnswer(surveyId, responseId, userId, answer);
            processedQuestionIds.add(answer.questionId());
            log.debug("질문 ID [{}] 처리 완료", answer.questionId());
        });

        return processedQuestionIds;
    }

    private void validateAnswerText(QuestionAnswerRequest answer) {
        String text = answer.text();
        if (text == null || text.trim().isEmpty()) {
            log.warn("빈 답변 텍스트 발견 - 질문 ID: {}", answer.questionId());
            throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
        }
    }

    /**
     * 하나의 QuestionAnswerRequest에 대해 텍스트 감성 분석을 수행하고 개별 감성 보고서를 저장합니다.
     */
    private void processAnswer(Long surveyId, Long responseId, Long userId, QuestionAnswerRequest answer) {
        String text = answer.text();
        log.debug("질문 ID [{}]에 대한 감성 분석 시작", answer.questionId());

        // 텍스트 감성 분석 수행
        SentimentStats stats = analyzeText(text);
        if (stats.getTotal() == 0) {
            log.warn("감성 분석 결과 총합이 0입니다. 질문 ID: {}", answer.questionId());
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
        log.info("개별 감성 보고서 저장 완료 - 질문 ID: {}, 응답 ID: {}", answer.questionId(), responseId);
    }

    /**
     * 특정 설문 및 질문에 대해 개별 보고서들을 조회한 후, 전체 통계 보고서를 생성하거나 업데이트합니다.
     */
    private void generateOverallReportForQuestion(Long surveyId, Long questionId) {
        List<SentimentReport> reports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId);
        if (reports.isEmpty()) {
            log.warn("설문 ID [{}]의 질문 ID [{}]에 해당하는 개별 보고서가 존재하지 않습니다", surveyId, questionId);
            throw new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND);
        }

        log.debug("설문 ID [{}], 질문 ID [{}]에 대한 개별 보고서 총 {}건 조회됨", surveyId, questionId, reports.size());
        // 개별 보고서들을 기반으로 전체 통계(가중 평균 등)를 계산
        OverallStats stats = OverallStats.fromReports(reports);
        log.debug("집계 통계 계산 완료 - 총 응답 수: {}, 긍정: {}, 부정: {} 등", stats.getTotalResponses(), stats.getPositiveCount(), stats.getNegativeCount());

        OverallSentimentReport overallReport;
        if (overallReportRepository.existsBySurveyIdAndQuestionId(surveyId, questionId)) {
            overallReport = overallReportRepository.findBySurveyIdAndQuestionId(surveyId, questionId)
                                                   .orElseThrow(() -> {
                                                       log.warn("전체 감성 보고서를 찾을 수 없습니다 - 설문 ID: {}, 질문 ID: {}", surveyId, questionId);
                                                       return new NotFoundException(ReportExceptionType.OVERALL_REPORT_NOT_FOUND);
                                                   });
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
            log.info("전체 감성 보고서 업데이트 완료 - 설문 ID: {}, 질문 ID: {}", surveyId, questionId);
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
            log.info("전체 감성 보고서 생성 완료 - 설문 ID: {}, 질문 ID: {}", surveyId, questionId);
        }

        // 개별 보고서와의 연관관계 동기화
        for (SentimentReport child : reports) {
            child.addOverallSentimentReportAndSentimentReport(overallReport);
        }
        sentimentReportRepository.saveAll(reports);
        log.debug("전체 감성 보고서와 개별 보고서 간 연관관계 동기화 완료 - 설문 ID: {}, 질문 ID: {}", surveyId, questionId);
    }

    /**
     * 텍스트를 기반으로 AWS Comprehend 또는 유사한 서비스를 호출하여 감성 분석 결과를 누적한 통계 데이터를 반환합니다.
     */
    private SentimentStats analyzeText(String text) {
        SentimentStats stats = new SentimentStats();
        if (text != null && !text.trim().isEmpty()) {
            log.debug("AWS 감성 분석 호출 시작");
            DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
            AWSComprehendResult result = AWSComprehendResult.from(dsr);
            stats.accumulate(result);
            log.debug("AWS 감성 분석 결과 누적 완료");
        } else {
            log.warn("감성 분석을 위한 텍스트가 비어있습니다.");
        }
        return stats;
    }

    public SentimentReportDto getAllSentimentReportBySurveyAndQuestion(Long surveyId, Long questionId, int page) {
        log.info("설문 ID [{}], 질문 ID [{}]에 대한 개별 감성 보고서 목록 조회 시작 (페이지 {})", surveyId, questionId, page);
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SentimentReport> sentimentReports = sentimentReportRepository.findAllBySurveyIdAndQuestionId(surveyId, questionId, pageable);

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(surveyId, questionId);
        log.debug("설문/질문 관련 정보 조회 완료: {}", questionWithSurveyDto);

        if (sentimentReports.isEmpty()) {
            log.warn("설문 ID [{}], 질문 ID [{}]에 해당하는 감성 보고서가 없습니다", surveyId, questionId);
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        Page<SentimentReportDetailDto> reportItems = sentimentReports.map(SentimentReportDetailDto::from);
        log.info("총 {}건의 감성 보고서 반환", reportItems.getTotalElements());
        return new SentimentReportDto(questionWithSurveyDto, reportItems);
    }

    public SentimentReportSingleDto getSentimentReport(Long sentimentId) {
        log.info("단일 감성 보고서 조회 시작 - 감성 보고서 ID: {}", sentimentId);
        SentimentReport sentimentReport = sentimentReportRepository.findById(sentimentId)
                                                                   .orElseThrow(() -> {
                                                                       log.warn("감성 보고서 ID [{}]를 찾을 수 없습니다", sentimentId);
                                                                       return new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND);
                                                                   });

        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(
                sentimentReport.getSurveyId(),
                sentimentReport.getQuestionId()
        );
        log.info("단일 감성 보고서 조회 완료 - 감성 보고서 ID: {}", sentimentId);
        return SentimentReportSingleDto.from(sentimentReport, questionWithSurveyDto);
    }

    public Page<OverallSentimentReportSummaryDto> getAllOverallReportBySurvey(Long surveyId, int page) {
        log.info("설문 ID [{}]에 대한 전체 감성 보고서 목록 조회 시작 (페이지 {})", surveyId, page);
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OverallSentimentReport> overallSentimentReports = overallReportRepository.findAllBySurveyId(surveyId, pageable);

        if (overallSentimentReports.isEmpty()) {
            log.warn("설문 ID [{}]에 해당하는 전체 감성 보고서가 없습니다", surveyId);
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        Page<OverallSentimentReportSummaryDto> summaryDtos = overallSentimentReports.map(entity -> {
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
        log.info("설문 ID [{}]의 전체 감성 보고서 {}건 조회 완료", surveyId, summaryDtos.getTotalElements());
        return summaryDtos;
    }

    public OverallSentimentReportDto getOverallReportById(Long overallReportId) {
        log.info("전체 감성 보고서 단건 조회 시작 - 전체 보고서 ID: {}", overallReportId);
        OverallSentimentReport overallSentimentReport = overallReportRepository.findById(overallReportId)
                                                                               .orElseThrow(() -> {
                                                                                   log.warn("전체 감성 보고서 ID [{}]를 찾을 수 없습니다", overallReportId);
                                                                                   return new NotFoundException(ReportExceptionType.OVERALL_REPORT_NOT_FOUND);
                                                                               });
        QuestionWithSurveyDto questionWithSurveyDto = surveyClientService.getQuestionWithSurvey(
                overallSentimentReport.getSurveyId(), overallSentimentReport.getQuestionId()
        );
        log.info("전체 감성 보고서 단건 조회 완료 - 전체 보고서 ID: {}", overallReportId);
        return OverallSentimentReportDto.from(overallSentimentReport, questionWithSurveyDto.title(), questionWithSurveyDto.questionText());
    }
}