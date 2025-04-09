package com.example.report_service.dto.request;

public record QuestionAnswerRequest(
        Long questionId,
        String text
) {}
