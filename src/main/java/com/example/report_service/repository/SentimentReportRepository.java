package com.example.report_service.repository;

import com.example.report_service.entity.SentimentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentimentReportRepository extends JpaRepository<SentimentReport, Long> {

    List<SentimentReport> findBySurveyId(Long surveyId);

    Page<SentimentReport> findBySurveyId(Long surveyId, Pageable pageable);
    
}

