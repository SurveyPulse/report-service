package com.example.report_service.repository;

import com.example.report_service.entity.OverallSentimentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverallSentimentReportRepository extends JpaRepository<OverallSentimentReport, Long> {

    Page<OverallSentimentReport> findBySurveyId(Long surveyId, Pageable pageable);

}
