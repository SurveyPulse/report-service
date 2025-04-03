package com.example.report_service.dto.response;

import com.example.report_service.entity.SentimentReport;

import java.time.LocalDateTime;

public record SentimentReportDto(
        Long sentimentId,
        Long surveyId,
        Long responseId,
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
    public static SentimentReportDto from(SentimentReport entity) {
        return new SentimentReportDto(
                entity.getId(),
                entity.getSurveyId(),
                entity.getResponseId(),
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
