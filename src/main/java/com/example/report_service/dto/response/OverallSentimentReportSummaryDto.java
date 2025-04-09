package com.example.report_service.dto.response;

public record OverallSentimentReportSummaryDto(
        Long overallReportId,
        String title,
        Long questionId,
        String questionText
) {
    public static OverallSentimentReportSummaryDto from(Long overallReportId,
                                                        String title,
                                                        Long questionId,
                                                        String questionText) {
        return new OverallSentimentReportSummaryDto(
                overallReportId,
                title,
                questionId,
                questionText
        );
    }
}

