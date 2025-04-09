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
    public ResponseEntity<Void> analyzeAndAggregateReport(@RequestBody AggregateRequest request) {
        reportService.aggregateReport(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/overall/{surveyId}/{questionId}/generate")
    public ResponseEntity<OverallSentimentReportDto> generateOverallReport(@PathVariable Long surveyId,
                                                                           @PathVariable Long questionId) {
        OverallSentimentReportDto overallReport = reportService.generateOverallReport(surveyId, questionId);
        return ResponseEntity.ok(overallReport);
    }

    @GetMapping("/sentiments/{surveyId}/{questionId}")
    public ResponseEntity<Page<SentimentReportDto>> getAllSentimentReport(@PathVariable Long surveyId,
                                                                        @PathVariable Long questionId,
                                                                        @RequestParam(defaultValue = "0") int page) {
        Page<SentimentReportDto> dtoPage = reportService.getAllSentimentReport(surveyId, questionId, page);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/overall/{surveyId}/{questionId}")
    public ResponseEntity<OverallSentimentReportDto> getOverallReport(@PathVariable Long surveyId,
                                                                      @PathVariable Long questionId) {
        OverallSentimentReportDto overallSentimentReportDto = reportService.getOverallReport(surveyId, questionId);
        return ResponseEntity.ok(overallSentimentReportDto);
    }

    @GetMapping("/sentiment/{sentimentId}")
    public ResponseEntity<SentimentReportDto> getSentimentReport(@PathVariable Long sentimentId) {
        SentimentReportDto sentimentReportDto = reportService.getSentimentReport(sentimentId);
        return ResponseEntity.ok(sentimentReportDto);
    }
}
