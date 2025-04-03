package com.example.report_service.dto.internal;

import com.example.report_service.entity.SentimentReport;
import lombok.Getter;

import java.util.List;

@Getter
public class OverallStats {

    private final int totalResponses, positiveCount, negativeCount, neutralCount, mixedCount;
    private final double overallPositive, overallNegative, overallNeutral, overallMixed;

    public OverallStats(int totalResponses, int positiveCount, int negativeCount, int neutralCount, int mixedCount,
                        double overallPositive, double overallNegative, double overallNeutral, double overallMixed) {
        this.totalResponses = totalResponses;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.mixedCount = mixedCount;
        this.overallPositive = overallPositive;
        this.overallNegative = overallNegative;
        this.overallNeutral = overallNeutral;
        this.overallMixed = overallMixed;
    }

    public static OverallStats fromReports(List<SentimentReport> reports) {
        int totalResponses = 0, pos = 0, neg = 0, neu = 0, mix = 0;
        double wSumPos = 0, wSumNeg = 0, wSumNeu = 0, wSumMix = 0;
        for (SentimentReport sr : reports) {
            totalResponses += sr.getTotalResponses();
            pos += sr.getPositiveCount();
            neg += sr.getNegativeCount();
            neu += sr.getNeutralCount();
            mix += sr.getMixedCount();
            wSumPos += sr.getAveragePositive() * sr.getTotalResponses();
            wSumNeg += sr.getAverageNegative() * sr.getTotalResponses();
            wSumNeu += sr.getAverageNeutral() * sr.getTotalResponses();
            wSumMix += sr.getAverageMixed() * sr.getTotalResponses();
        }
        double overallPos = totalResponses > 0 ? Math.floor((wSumPos / totalResponses) * 1000) / 1000.0 : 0.0;
        double overallNeg = totalResponses > 0 ? Math.floor((wSumNeg / totalResponses) * 1000) / 1000.0 : 0.0;
        double overallNeu = totalResponses > 0 ? Math.floor((wSumNeu / totalResponses) * 1000) / 1000.0 : 0.0;
        double overallMix = totalResponses > 0 ? Math.floor((wSumMix / totalResponses) * 1000) / 1000.0 : 0.0;
        return new OverallStats(totalResponses, pos, neg, neu, mix, overallPos, overallNeg, overallNeu, overallMix);
    }
}
