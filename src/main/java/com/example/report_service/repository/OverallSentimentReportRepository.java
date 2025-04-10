package com.example.report_service.repository;

import com.example.report_service.entity.OverallSentimentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverallSentimentReportRepository extends JpaRepository<OverallSentimentReport, Long> {

    Optional<OverallSentimentReport> findBySurveyIdAndQuestionId(Long surveyId, Long questionId);

    Page<OverallSentimentReport> findAll(Pageable pageable);

    boolean existsBySurveyIdAndQuestionId(Long surveyId, Long questionId);

    Page<OverallSentimentReport> findBySurveyId(Long surveyId, Pageable pageable);

}
