package com.example.report_service.service.messagequeue;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.service.SentimentReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregateRequestConsumer {

    private final SentimentReportService sentimentReportService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "aggregate-request-topic", groupId = "report-group")
    public void consume(String message) {
        log.info("Kafka 컨슈머가 메시지 수신: {}", message);
        try {
            // JSON 문자열을 AggregateRequest 객체로 변환
            AggregateRequest aggregateRequest = objectMapper.readValue(message, AggregateRequest.class);
            sentimentReportService.aggregateAndGenerateReport(aggregateRequest);
            log.info("AggregateRequest 처리 성공: {}", aggregateRequest);
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지에서 AggregateRequest 파싱 에러 발생", e);
        } catch (Exception ex) {
            log.error("Kafka 메시지 처리 중 예기치 않은 오류 발생", ex);
        }
    }
}
