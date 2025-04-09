package com.example.report_service.dto.response;

import java.util.List;

public record AggregatedSentimentReportDto(
        Long surveyId,
        Long responseId,
        List<QuestionSentimentReportDto> reports
) {}
