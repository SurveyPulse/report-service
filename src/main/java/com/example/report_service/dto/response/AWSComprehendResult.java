package com.example.report_service.dto.response;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AWSComprehendResult {
    private String sentiment; // "POSITIVE", "NEGATIVE", "NEUTRAL", "MIXED"
    private SentimentScore sentimentScore;

    public static AWSComprehendResult from(DetectSentimentResult result) {
        SentimentScore score = new SentimentScore(
                result.getSentimentScore().getPositive(),
                result.getSentimentScore().getNegative(),
                result.getSentimentScore().getNeutral(),
                result.getSentimentScore().getMixed()
        );
        return new AWSComprehendResult(result.getSentiment(), score);
    }
}
