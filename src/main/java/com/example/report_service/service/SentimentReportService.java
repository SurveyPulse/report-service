package com.example.report_service.service;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.example.report_service.dto.response.AWSComprehendResult;
import com.example.report_service.entity.SentimentReport;
import com.example.report_service.repository.SentimentReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SentimentReportService {

    private final SentimentReportRepository reportRepository;
    private final AwsComprehendService awsComprehendService;

    public SentimentReportService(SentimentReportRepository reportRepository,
                                  AwsComprehendService awsComprehendService) {
        this.reportRepository = reportRepository;
        this.awsComprehendService = awsComprehendService;
    }

    /**
     * 클라이언트가 전달한 텍스트 목록에 대해 감성 분석을 수행하고, 결과를 집계하여 보고서를 생성, 저장합니다.
     * responseId는 외부 연계가 없으므로 임의로 0L로 설정합니다.
     *
     * @param surveyId 분석 대상 설문 ID
     * @param texts 분석할 텍스트 목록
     * @return 저장된 SentimentReport 엔티티
     */
    @Transactional
    public SentimentReport aggregateReport(Long surveyId, List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("분석할 텍스트 목록이 비어있습니다.");
        }

        List<AWSComprehendResult> results = texts.stream()
                                                 .filter(text -> text != null && !text.trim().isEmpty())
                                                 .map(text -> {
                                                     DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
                                                     return AWSComprehendResult.from(dsr);
                                                 })
                                                 .collect(Collectors.toList());

        return aggregateAndSave(surveyId, 0L, results);
    }

    /**
     * AWSComprehendResult 목록을 집계하여 보고서를 생성, 저장합니다.
     *
     * @param surveyId 분석 대상 설문 ID
     * @param responseId 분석에 사용된 응답 ID (외부 연계가 없으므로 0L로 처리)
     * @param results 감성 분석 결과 리스트
     * @return 저장된 SentimentReport 엔티티
     */
    @Transactional
    public SentimentReport aggregateAndSave(Long surveyId, Long responseId, List<AWSComprehendResult> results) {
        int total = results.size();
        int positiveCount = 0, negativeCount = 0, neutralCount = 0, mixedCount = 0;
        double sumPositive = 0.0, sumNegative = 0.0, sumNeutral = 0.0, sumMixed = 0.0;

        for (AWSComprehendResult result : results) {
            String sentiment = result.getSentiment();
            if ("POSITIVE".equalsIgnoreCase(sentiment)) {
                positiveCount++;
            } else if ("NEGATIVE".equalsIgnoreCase(sentiment)) {
                negativeCount++;
            } else if ("NEUTRAL".equalsIgnoreCase(sentiment)) {
                neutralCount++;
            } else if ("MIXED".equalsIgnoreCase(sentiment)) {
                mixedCount++;
            }
            sumPositive += result.getSentimentScore().getPositive();
            sumNegative += result.getSentimentScore().getNegative();
            sumNeutral += result.getSentimentScore().getNeutral();
            sumMixed += result.getSentimentScore().getMixed();
        }

        // 평균 계산 후 소수점 3자리 이하 버리기 (내림)
        double avgPositive = Math.floor((sumPositive / total) * 1000) / 1000.0;
        double avgNegative = Math.floor((sumNegative / total) * 1000) / 1000.0;
        double avgNeutral  = Math.floor((sumNeutral  / total) * 1000) / 1000.0;
        double avgMixed    = Math.floor((sumMixed    / total) * 1000) / 1000.0;

        SentimentReport report = SentimentReport.builder()
                                                .surveyId(surveyId)
                                                .responseId(responseId)
                                                .totalResponses(total)
                                                .positiveCount(positiveCount)
                                                .negativeCount(negativeCount)
                                                .neutralCount(neutralCount)
                                                .mixedCount(mixedCount)
                                                .averagePositive(avgPositive)
                                                .averageNegative(avgNegative)
                                                .averageNeutral(avgNeutral)
                                                .averageMixed(avgMixed)
                                                .generatedAt(LocalDateTime.now())
                                                .build();

        SentimentReport savedReport = reportRepository.save(report);
        log.info("Sentiment report saved with id: {}", savedReport.getId());
        return savedReport;
    }
}
