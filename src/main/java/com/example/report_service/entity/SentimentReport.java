package com.example.report_service.entity;

import com.example.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sentiment_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentimentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long surveyId;

    @Column(nullable = false)
    private Long responseId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overall_sentiment_report_id")
    private OverallSentimentReport overallSentimentReport;

    @Column(nullable = false)
    private int totalResponses;

    @Column(nullable = false)
    private int positiveCount;
    @Column(nullable = false)
    private int negativeCount;
    @Column(nullable = false)
    private int neutralCount;
    @Column(nullable = false)
    private int mixedCount;

    // 각 감성 점수의 평균
    @Column(nullable = false)
    private double averagePositive;
    @Column(nullable = false)
    private double averageNegative;
    @Column(nullable = false)
    private double averageNeutral;
    @Column(nullable = false)
    private double averageMixed;

    @Builder
    public SentimentReport(Long surveyId, Long responseId, Long questionId, Long userId, int totalResponses, int positiveCount, int negativeCount,
                           int neutralCount, int mixedCount, double averagePositive, double averageNegative, double averageNeutral,
                           double averageMixed) {
        this.surveyId = surveyId;
        this.responseId = responseId;
        this.questionId = questionId;
        this.userId = userId;
        this.totalResponses = totalResponses;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.mixedCount = mixedCount;
        this.averagePositive = averagePositive;
        this.averageNegative = averageNegative;
        this.averageNeutral = averageNeutral;
        this.averageMixed = averageMixed;
    }

    public void addOverallSentimentReportAndSentimentReport(OverallSentimentReport overallSentimentReport) {
        this.overallSentimentReport = overallSentimentReport;
        overallSentimentReport.getSentimentReports().add(this);
    }
}
