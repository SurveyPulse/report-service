package com.example.report_service.service.messagequeue;

import com.example.report_service.dto.request.AggregateRequest;
import com.example.report_service.service.SentimentReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregateRequestConsumer {

    private final SentimentReportService sentimentReportService;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor consumerExecutor;

    @KafkaListener(
            topics = "aggregate-request-topic",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBatch(List<String> messages, Acknowledgment ack) {
        log.info("Kafka 컨슈머가 {}개의 메시지를 수신했습니다.", messages.size());

        consumerExecutor.execute(() -> {
            for (String message : messages) {
                try {
                    AggregateRequest req = objectMapper.readValue(message, AggregateRequest.class);
                    sentimentReportService.aggregateAndGenerateReport(req);
                    log.debug("AggregateRequest 처리 성공: {}", req);
                } catch (JsonProcessingException e) {
                    log.error("메시지 파싱 오류: {}", message, e);
                } catch (Exception ex) {
                    log.error("AggregateRequest 처리 중 예기치 않은 오류 발생: {}", message, ex);
                    // 필요하면 여기서 throw ex; 로 ErrorHandler로 넘길 수도 있습니다.
                }
            }
            // 배치 처리 후 한 번만 수동 커밋
            ack.acknowledge();
            log.info("{}개의 메시지를 처리하고 오프셋을 커밋했습니다.", messages.size());
        });
    }
}
