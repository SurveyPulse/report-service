package com.example.report_service.controller;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.response.OverallSentimentReportDto;
import com.example.report_service.dto.response.SentimentReportDto;
import com.example.report_service.service.SentimentReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class SentimentReportController {

    private final SentimentReportService reportService;

    @PostMapping("/analyze")
    public ResponseEntity<SentimentReportDto> analyzeAndAggregateReport(@RequestBody AggregateRequest request) {
            SentimentReportDto report = reportService.aggregateReport(request);
            return ResponseEntity.ok(report);
    }

    @PostMapping("/overall/{surveyId}/generate")
    public ResponseEntity<OverallSentimentReportDto> generateOverallReport(@PathVariable Long surveyId) {
            OverallSentimentReportDto overallReport = reportService.generateOverallReport(surveyId);
            return ResponseEntity.ok(overallReport);
    }

    @GetMapping("/overall/{surveyId}")
    public ResponseEntity<Page<OverallSentimentReportDto>> getOverallReports(@PathVariable Long surveyId, @RequestParam(defaultValue = "0") int page) {
            Page<OverallSentimentReportDto> dtoPage = reportService.getOverallReports(surveyId, page);
            return ResponseEntity.ok(dtoPage);
    }
}
