package com.example.report_service.controller;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.response.OverallSentimentReportDto;
import com.example.report_service.dto.response.SentimentReportDto;
import com.example.report_service.service.SentimentReportService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
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

    @GetMapping("/sentiments/{surveyId}")
    public ResponseEntity<Page<SentimentReportDto>> getSentimentReports(@PathVariable Long surveyId, @RequestParam(defaultValue = "0") int page) {
            Page<SentimentReportDto> dtoPage = reportService.getSentimentReports(surveyId, page);
            return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/overall/{surveyId}")
    public ResponseEntity<OverallSentimentReportDto> getOverallReport(@PathVariable Long surveyId) {
        OverallSentimentReportDto overallSentimentReportDto = reportService.getOverallReport(surveyId);
        return ResponseEntity.ok(overallSentimentReportDto);
    }
}
