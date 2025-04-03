package com.example.report_service.repository;


import com.example.report_service.entity.SentimentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentimentReportRepository extends JpaRepository<SentimentReport, Long> {
}
