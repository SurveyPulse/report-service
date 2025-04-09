package com.example.report_service.dto.response;

import java.time.LocalDateTime;

public record QuestionSentimentReportDto(
        Long questionId,
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
    public static QuestionSentimentReportDto from(SentimentReportDto dto) {
        return new QuestionSentimentReportDto(
                dto.questionId(),
                dto.totalResponses(),
                dto.positiveCount(),
                dto.negativeCount(),
                dto.neutralCount(),
                dto.mixedCount(),
                dto.averagePositive(),
                dto.averageNegative(),
                dto.averageNeutral(),
                dto.averageMixed(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }
}
