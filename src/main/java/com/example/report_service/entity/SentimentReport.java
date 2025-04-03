package com.example.report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentimentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석 대상 설문 ID (필요에 따라 사용)
    @Column(nullable = false)
    private Long surveyId;

    @Column(nullable = false)
    private Long responseId;

    // 전체 응답 수
    @Column(nullable = false)
    private int totalResponses;

    // 감성 유형별 응답 개수
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

    // 보고서 생성 시각
    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Builder
    public SentimentReport(Long surveyId, Long responseId, int totalResponses, int positiveCount, int negativeCount,
                           int neutralCount, int mixedCount, double averagePositive, double averageNegative, double averageNeutral,
                           double averageMixed, LocalDateTime generatedAt) {
        this.surveyId = surveyId;
        this.responseId = responseId;
        this.totalResponses = totalResponses;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.neutralCount = neutralCount;
        this.mixedCount = mixedCount;
        this.averagePositive = averagePositive;
        this.averageNegative = averageNegative;
        this.averageNeutral = averageNeutral;
        this.averageMixed = averageMixed;
        this.generatedAt = generatedAt;
    }
}
