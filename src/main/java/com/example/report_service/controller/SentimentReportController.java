package com.example.report_service.controller;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.entity.SentimentReport;
import com.example.report_service.service.SentimentReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Slf4j
public class SentimentReportController {

    private final SentimentReportService reportService;

    public SentimentReportController(SentimentReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * POST /api/reports/analyze
     * 요청 본문에 surveyId와 분석할 텍스트 목록을 받아, 감성 분석을 수행하고 집계 보고서를 생성합니다.
     */
    @PostMapping("/analyze")
    public ResponseEntity<SentimentReport> analyzeAndAggregateReport(@RequestBody AggregateRequest request) {
        try {
            SentimentReport report = reportService.aggregateReport(request.getSurveyId(), request.getTexts());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error aggregating report for surveyId {}", request.getSurveyId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
