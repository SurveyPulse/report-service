package com.example.report_service.entity;

import com.example.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "overall_sentiment_reports",
        indexes = {
                @Index(name = "idx_survey", columnList = "surveyId")
        }
)
public class OverallSentimentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long surveyId;

    @Column(nullable = false)
    private Long questionId;

    @OneToMany(mappedBy = "overallSentimentReport", cascade = CascadeType.PERSIST)
    List<SentimentReport> sentimentReports = new ArrayList<>();

    @Column(nullable = false)
    private int totalResponses;

    // 감성 점수의 평균
    @Column(nullable = false)
    private double averagePositive;
    @Column(nullable = false)
    private double averageNegative;
    @Column(nullable = false)
    private double averageNeutral;
    @Column(nullable = false)
    private double averageMixed;

    @Column(nullable = false)
    private int positiveCount;
    @Column(nullable = false)
    private int negativeCount;
    @Column(nullable = false)
    private int neutralCount;
    @Column(nullable = false)
    private int mixedCount;

    @Builder
    public OverallSentimentReport(Long surveyId, Long questionId, int totalResponses, double averagePositive,
                                  double averageNegative, double averageNeutral, double averageMixed,
                                  int positiveCount, int negativeCount, int neutralCount, int mixedCount) {
        this.surveyId = surveyId;
        this.questionId = questionId;
        this.totalResponses = totalResponses;
        this.averagePositive = averagePositive;
        this.averageNegative = averageNegative;
        this.averageNeutral = averageNeutral;
        this.averageMixed = averageMixed;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.mixedCount = mixedCount;
    }

    public void updateStats(int totalResponses, int positiveCount, int negativeCount, int neutralCount, int mixedCount,
                            double averagePositive, double averageNegative, double averageNeutral, double averageMixed) {
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
}
