package com.example.report_service.client;

import com.example.report_service.client.config.FeignClientConfig;
import com.example.report_service.dto.response.QuestionWithSurveyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "survey-service", url = "${feign.survey-service-url}", configuration = FeignClientConfig.class)
public interface SurveyClient {

    @GetMapping("/api/surveys/{surveyId}/{questionId}")
    QuestionWithSurveyDto getQuestionWithSurvey(@PathVariable Long surveyId, @PathVariable Long questionId);

}
