package com.example.report_service.entity;

import com.example.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "overall_sentiment_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OverallSentimentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석 대상 설문 ID
    @Column(nullable = false)
    private Long surveyId;

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
    public OverallSentimentReport(Long surveyId, int totalResponses, double averagePositive,
                                  double averageNegative, double averageNeutral, double averageMixed,
                                  int positiveCount, int negativeCount, int neutralCount, int mixedCount) {
        this.surveyId = surveyId;
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
}
