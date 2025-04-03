package com.example.report_service.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AggregateRequest {
    private Long surveyId;
    private Long responseId;
    private List<String> texts;
}
