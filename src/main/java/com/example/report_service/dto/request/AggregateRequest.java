package com.example.report_service.dto.request;

import java.util.List;

public record AggregateRequest(
        Long surveyId,
        Long responseId,
        Long userId,
        List<QuestionAnswerRequest> answers
) {}
