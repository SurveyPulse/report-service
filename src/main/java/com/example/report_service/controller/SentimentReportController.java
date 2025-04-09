package com.example.report_service.controller;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.dto.response.OverallSentimentReportDto;
import com.example.report_service.dto.response.OverallSentimentReportSummaryDto;
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
    public ResponseEntity<Void> generateOverallReport(@PathVariable Long surveyId,
                                                                           @PathVariable Long questionId) {
        reportService.generateOverallReport(surveyId, questionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sentiments/{surveyId}/{questionId}")
    public ResponseEntity<Page<SentimentReportDto>> getAllSentimentReport(@PathVariable Long surveyId,
                                                                        @PathVariable Long questionId,
                                                                        @RequestParam(defaultValue = "0") int page) {
        Page<SentimentReportDto> dtoPage = reportService.getAllSentimentReportBySurveyAndQuestion(surveyId, questionId, page);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/overall/{overallReportId}")
    public ResponseEntity<OverallSentimentReportDto> getOverallReport(@PathVariable Long overallReportId) {
        OverallSentimentReportDto overallSentimentReportDto = reportService.getOverallReportById(overallReportId);
        return ResponseEntity.ok(overallSentimentReportDto);
    }

    @GetMapping("/overalls/{surveyId}")
    public ResponseEntity<Page<OverallSentimentReportSummaryDto>> getAllOverallSentimentReportBySurvey(
            @PathVariable Long surveyId,
            @RequestParam(defaultValue = "0") int page) {
        Page<OverallSentimentReportSummaryDto> result = reportService.getAllOverallReportBySurvey(surveyId, page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sentiment/{sentimentId}")
    public ResponseEntity<SentimentReportDto> getSentimentReport(@PathVariable Long sentimentId) {
        SentimentReportDto sentimentReportDto = reportService.getSentimentReport(sentimentId);
        return ResponseEntity.ok(sentimentReportDto);
    }
}
