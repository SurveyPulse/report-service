package com.example.report_service.client.service;

import com.example.report_service.client.SurveyClient;
import com.example.report_service.dto.response.QuestionWithSurveyDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyClientService {

    private final SurveyClient surveyClient;

    @CircuitBreaker(name = "surveyService", fallbackMethod = "fallbackGetQuestionWithSurvey")
    @Retry(name = "surveyService", fallbackMethod = "fallbackGetQuestionWithSurvey")
    public QuestionWithSurveyDto getQuestionWithSurvey(Long surveyId, Long questionId) {
        return surveyClient.getQuestionWithSurvey(surveyId, questionId);
    }

    public QuestionWithSurveyDto fallbackGetQuestionWithSurvey(Long surveyId, Long questionId, Throwable throwable) {
        log.error("SurveyClient 호출 실패. surveyId: {}, questionId: {}. 기본값을 반환합니다.", surveyId, questionId, throwable);

        return new QuestionWithSurveyDto(
                questionId,
                "기본 질문 내용을 확인할 수 없습니다.",
                surveyId,
                "기본 설문 제목",
                "해당 설문에 대한 상세 정보를 불러올 수 없습니다.",
                0L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "UNKNOWN"
        );
    }
}
