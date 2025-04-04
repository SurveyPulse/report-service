package com.example.report_service.service;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.example.global.exception.type.NotFoundException;
import com.example.report_service.dto.internal.OverallStats;
import com.example.report_service.dto.internal.SentimentStats;
import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.response.AWSComprehendResult;
import com.example.report_service.dto.response.OverallSentimentReportDto;
import com.example.report_service.dto.response.SentimentReportDto;
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
            throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
        }

        // 2) 텍스트 감성 분석 (SentimentStats 사용)
        SentimentStats stats = analyzeTexts(texts);
        if (stats.getTotal() == 0) {
            throw new NotFoundException(ReportExceptionType.TEXTS_IS_EMPTY);
        }

        // 3) 평균 계산 (SentimentStats 내 계산 메서드 사용)
        double avgPositive = stats.getAvgPositive();
        double avgNegative = stats.getAvgNegative();
        double avgNeutral  = stats.getAvgNeutral();
        double avgMixed    = stats.getAvgMixed();

        // 4) 개별 보고서 엔티티 생성
        SentimentReport report = SentimentReport.builder()
                                                .surveyId(aggregateRequest.getSurveyId())
                                                .responseId(aggregateRequest.getResponseId() != null ? aggregateRequest.getResponseId() : 0L)
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

        SentimentReport savedReport = reportRepository.save(report);

        SentimentReportDto sentimentReportDto = SentimentReportDto.from(savedReport);
        return sentimentReportDto;
    }

    private SentimentStats analyzeTexts(List<String> texts) {
        SentimentStats stats = new SentimentStats();
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) continue;
            DetectSentimentResult dsr = awsComprehendService.analyzeText(text);
            AWSComprehendResult result = AWSComprehendResult.from(dsr);
            stats.accumulate(result);
        }
        return stats;
    }

    @Transactional
    public OverallSentimentReportDto generateOverallReport(Long surveyId) {
        List<SentimentReport> reports = reportRepository.findBySurveyId(surveyId);
        if (reports.isEmpty()) {
            throw new NotFoundException(ReportExceptionType.REPORT_NOT_FOUND);
        }

        // 2) 가중 평균 계산
        OverallStats stats = OverallStats.fromReports(reports);

        OverallSentimentReport overallReport = OverallSentimentReport.builder()
                                                                     .surveyId(surveyId)
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

        OverallSentimentReport savedOverall = overallReportRepository.save(overallReport);
        for (SentimentReport child : reports) {
            child.addOverallSentimentReportAndSentimentReport(savedOverall);
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
            throw new NotFoundException(ReportExceptionType.OVERALL_SENTIMENT_IS_EMPTY);
        }

        Page<OverallSentimentReportDto> dtoPage = overallList.map(OverallSentimentReportDto::from);

        return dtoPage;
    }

}
