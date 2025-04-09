package com.example.report_service.dto.response;

import java.time.LocalDateTime;

public record QuestionWithSurveyDto(
        Long questionId,
        String questionText,
        Long surveyId,
        String title,
        String description,
        Long creatorUserId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {
}
