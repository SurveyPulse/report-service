package com.example.report_service.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AggregateRequest {
    private Long surveyId;
    private List<String> texts;
}
