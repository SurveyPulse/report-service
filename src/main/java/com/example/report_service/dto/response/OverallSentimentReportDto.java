package com.example.report_service.dto.response;

import com.example.report_service.entity.OverallSentimentReport;

import java.time.LocalDateTime;

public record OverallSentimentReportDto(
        Long overallId,
        Long surveyId,
        int totalResponses,
        int positiveCount,
        int negativeCount,
        int neutralCount,
        int mixedCount,
        double averagePositive,
        double averageNegative,
        double averageNeutral,
        double averageMixed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OverallSentimentReportDto from(OverallSentimentReport entity) {
        return new OverallSentimentReportDto(
                entity.getId(),
                entity.getSurveyId(),
                entity.getTotalResponses(),
                entity.getPositiveCount(),
                entity.getNegativeCount(),
                entity.getNeutralCount(),
                entity.getMixedCount(),
                entity.getAveragePositive(),
                entity.getAverageNegative(),
                entity.getAverageNeutral(),
                entity.getAverageMixed(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
