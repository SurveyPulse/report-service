package com.example.report_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SentimentScore {
    private double positive;
    private double negative;
    private double neutral;
    private double mixed;
}
