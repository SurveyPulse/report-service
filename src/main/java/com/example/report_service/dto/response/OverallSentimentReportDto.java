package com.example.report_service.dto.response;

import com.example.report_service.entity.OverallSentimentReport;

import java.time.LocalDateTime;
import java.util.List;

public record OverallSentimentReportDto(
        Long id,
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
        LocalDateTime updatedAt,
        List<SentimentReportDto> sentimentReports
) {
    public static OverallSentimentReportDto from(OverallSentimentReport entity) {
        var childDtos = entity.getSentimentReports().stream()
                              .map(SentimentReportDto::from)
                              .toList();

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
                entity.getUpdatedAt(),
                childDtos
        );
    }
}
