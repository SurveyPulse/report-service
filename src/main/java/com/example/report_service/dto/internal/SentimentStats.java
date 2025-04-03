package com.example.report_service.dto.internal;

import com.example.report_service.dto.response.AWSComprehendResult;
import lombok.Getter;

@Getter
public class SentimentStats {

    private int total, positiveCount, negativeCount, neutralCount, mixedCount;
    private double sumPositive, sumNegative, sumNeutral, sumMixed;

    public void accumulate(AWSComprehendResult result) {
        total++;
        switch (result.getSentiment().toUpperCase()) {
            case "POSITIVE" -> positiveCount++;
            case "NEGATIVE" -> negativeCount++;
            case "NEUTRAL" -> neutralCount++;
            case "MIXED" -> mixedCount++;
        }
        sumPositive += result.getSentimentScore().getPositive();
        sumNegative += result.getSentimentScore().getNegative();
        sumNeutral += result.getSentimentScore().getNeutral();
        sumMixed += result.getSentimentScore().getMixed();
    }

    public double getAvgPositive() {
        return total == 0 ? 0.0 : Math.floor((sumPositive / total) * 1000) / 1000.0;
    }

    public double getAvgNegative() {
        return total == 0 ? 0.0 : Math.floor((sumNegative / total) * 1000) / 1000.0;
    }

    public double getAvgNeutral() {
        return total == 0 ? 0.0 : Math.floor((sumNeutral / total) * 1000) / 1000.0;
    }

    public double getAvgMixed() {
        return total == 0 ? 0.0 : Math.floor((sumMixed / total) * 1000) / 1000.0;
    }
}
