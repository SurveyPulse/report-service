package com.example.report_service.repository;

import com.example.report_service.entity.OverallSentimentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OverallSentimentReportRepository extends JpaRepository<OverallSentimentReport, Long> {

    Optional<OverallSentimentReport> findBySurveyIdAndQuestionId(Long surveyId, Long questionId);

    Page<OverallSentimentReport> findAll(Pageable pageable);

    boolean existsBySurveyIdAndQuestionId(Long surveyId, Long questionId);

    @Query(
            value = """
        SELECT *
          FROM overall_sentiment_reports
        IGNORE INDEX (idx_survey)
         WHERE surveyId = :surveyId
        ORDER BY id
        """,
            countQuery = """
        SELECT COUNT(*)
          FROM overall_sentiment_reports
         WHERE surveyId = :surveyId
        """,
            nativeQuery = true
    )
    Page<OverallSentimentReport> findBySurveyIdIgnoreIndex(@Param("surveyId") Long surveyId, Pageable pageable);
}
