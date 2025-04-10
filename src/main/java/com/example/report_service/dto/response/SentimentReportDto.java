package com.example.report_service.dto.response;

import org.springframework.data.domain.Page;

public record SentimentReportDto(
        QuestionWithSurveyDto questionWithSurveyDto,
        Page<SentimentReportDetailDto> reports
) {}
