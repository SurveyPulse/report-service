package com.example.report_service.service;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.response.AWSComprehendResult;
import com.example.report_service.dto.response.OverallSentimentReportDto;
import com.example.report_service.dto.response.SentimentReportDto;
import com.example.report_service.entity.OverallSentimentReport;
import com.example.report_service.entity.SentimentReport;
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

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SentimentReportService {

    private final SentimentReportRepository reportRepository;
    private final OverallSentimentReportRepository overallReportRepository;
    private final AwsComprehendService awsComprehendService;

    @Transactional
    public SentimentReportDto aggregateReport(AggregateRequest aggregateRequest) {
        // 1) 텍스트 목록 검증
        List<String> texts = aggregateRequest.getTexts();
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("분석할 텍스트 목록이 비어있습니다.");
        }

        // 2) 감성 분석 누적
        int total = 0;
        int positiveCount = 0, negativeCount = 0, neutralCount = 0, mixedCount = 0;
        double sumPositive = 0.0, sumNegative = 0.0, sumNeutral = 0.0, sumMixed = 0.0;

        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) continue;
            total++;

            DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
            AWSComprehendResult result = AWSComprehendResult.from(dsr);

            // 감성 유형 카운트
            String sentiment = result.getSentiment();
            if ("POSITIVE".equalsIgnoreCase(sentiment)) positiveCount++;
            else if ("NEGATIVE".equalsIgnoreCase(sentiment)) negativeCount++;
            else if ("NEUTRAL".equalsIgnoreCase(sentiment)) neutralCount++;
            else if ("MIXED".equalsIgnoreCase(sentiment)) mixedCount++;

            // 감성 점수 합
            sumPositive += result.getSentimentScore().getPositive();
            sumNegative += result.getSentimentScore().getNegative();
            sumNeutral += result.getSentimentScore().getNeutral();
            sumMixed += result.getSentimentScore().getMixed();
        }

        if (total == 0) {
            throw new IllegalArgumentException("분석 가능한 텍스트가 없습니다.");
        }

        // 3) 평균 계산
        double avgPositive = Math.floor((sumPositive / total) * 1000) / 1000.0;
        double avgNegative = Math.floor((sumNegative / total) * 1000) / 1000.0;
        double avgNeutral  = Math.floor((sumNeutral  / total) * 1000) / 1000.0;
        double avgMixed    = Math.floor((sumMixed    / total) * 1000) / 1000.0;

        // 4) 개별 보고서 엔티티 생성
        SentimentReport report = SentimentReport.builder()
                                                .surveyId(aggregateRequest.getSurveyId())
                                                .responseId(aggregateRequest.getResponseId() != null ? aggregateRequest.getResponseId() : 0L)
                                                .totalResponses(total)
                                                .positiveCount(positiveCount)
                                                .negativeCount(negativeCount)
                                                .neutralCount(neutralCount)
                                                .mixedCount(mixedCount)
                                                .averagePositive(avgPositive)
                                                .averageNegative(avgNegative)
                                                .averageNeutral(avgNeutral)
                                                .averageMixed(avgMixed)
                                                .build();

        SentimentReport savedReport = reportRepository.save(report);

        SentimentReportDto sentimentReportDto = SentimentReportDto.from(savedReport);
        return sentimentReportDto;
    }

    /**
     * 전체 평균 보고서 생성
     * 1) 동일 surveyId의 모든 SentimentReport 조회
     * 2) 가중 평균 계산 후 OverallSentimentReport 생성
     * 3) 개별 보고서들에 대해 addOverallSentimentReportAndSentimentReport(...) 호출로 연관관계 동기화
     */
    @Transactional
    public OverallSentimentReportDto generateOverallReport(Long surveyId) {
        List<SentimentReport> reports = reportRepository.findBySurveyId(surveyId);
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("해당 설문에 대한 개별 보고서가 존재하지 않습니다.");
        }

        // 2) 가중 평균 계산
        int totalResponses = reports.stream().mapToInt(SentimentReport::getTotalResponses).sum();
        int positiveCount  = reports.stream().mapToInt(SentimentReport::getPositiveCount).sum();
        int negativeCount  = reports.stream().mapToInt(SentimentReport::getNegativeCount).sum();
        int neutralCount   = reports.stream().mapToInt(SentimentReport::getNeutralCount).sum();
        int mixedCount     = reports.stream().mapToInt(SentimentReport::getMixedCount).sum();

        double weightedSumPositive = reports.stream().mapToDouble(r -> r.getAveragePositive() * r.getTotalResponses()).sum();
        double weightedSumNegative = reports.stream().mapToDouble(r -> r.getAverageNegative() * r.getTotalResponses()).sum();
        double weightedSumNeutral  = reports.stream().mapToDouble(r -> r.getAverageNeutral()  * r.getTotalResponses()).sum();
        double weightedSumMixed    = reports.stream().mapToDouble(r -> r.getAverageMixed()    * r.getTotalResponses()).sum();

        double overallPositive = Math.floor((weightedSumPositive / totalResponses) * 1000) / 1000.0;
        double overallNegative = Math.floor((weightedSumNegative / totalResponses) * 1000) / 1000.0;
        double overallNeutral  = Math.floor((weightedSumNeutral  / totalResponses) * 1000) / 1000.0;
        double overallMixed    = Math.floor((weightedSumMixed    / totalResponses) * 1000) / 1000.0;

        OverallSentimentReport overallReport = OverallSentimentReport.builder()
                                                                     .surveyId(surveyId)
                                                                     .totalResponses(totalResponses)
                                                                     .averagePositive(overallPositive)
                                                                     .averageNegative(overallNegative)
                                                                     .averageNeutral(overallNeutral)
                                                                     .averageMixed(overallMixed)
                                                                     .positiveCount(positiveCount)
                                                                     .negativeCount(negativeCount)
                                                                     .neutralCount(neutralCount)
                                                                     .mixedCount(mixedCount)
                                                                     .build();

        OverallSentimentReport savedOverall = overallReportRepository.save(overallReport);

        for (SentimentReport sentimentReport : reports) {
            sentimentReport.addOverallSentimentReportAndSentimentReport(savedOverall);
        }
        reportRepository.saveAll(reports);

        OverallSentimentReportDto overallSentimentReportDto = OverallSentimentReportDto.from(savedOverall);

        return overallSentimentReportDto;
    }

    @Transactional(readOnly = true)
    public Page<OverallSentimentReportDto> getOverallReports(Long surveyId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OverallSentimentReport> overallList = overallReportRepository.findBySurveyId(surveyId, pageable);

        if (overallList.isEmpty()) {
            throw new IllegalArgumentException("전체 평균 보고서가 존재하지 않습니다.");
        }

        Page<OverallSentimentReportDto> dtoPage = overallList.map(OverallSentimentReportDto::from);

        return dtoPage;
    }

}
