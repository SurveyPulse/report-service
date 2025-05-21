package com.example.report_service.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AwsComprehendService {

    private final AmazonComprehend comprehendClient;

    public AwsComprehendService(
            @Value("${aws.region}") String region,
            @Value("${aws.credentials.access-key:}") String accessKey,
            @Value("${aws.credentials.secret-key:}") String secretKey
    ) {
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            this.comprehendClient = AmazonComprehendClientBuilder.standard()
                                                                 .withRegion(region)
                                                                 .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                                                 .build();
            log.info("정적 자격 증명으로 AWS Comprehend 클라이언트가 생성되었습니다. region={}, accessKey=****", region);
        } else {
            this.comprehendClient = AmazonComprehendClientBuilder.standard()
                                                                 .withRegion(region)
                                                                 .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                                                                 .build();
            log.info("기본 자격 증명 공급자 체인으로 AWS Comprehend 클라이언트가 생성되었습니다. region={}", region);
        }
    }

    public DetectSentimentResult analyzeText(String text) {
        try {
            DetectSentimentRequest request = new DetectSentimentRequest()
                    .withText(text)
                    .withLanguageCode("ko");
            DetectSentimentResult result = comprehendClient.detectSentiment(request);
            log.info("AWS Comprehend 분석 결과: 감성={}, 긍정점수={}, 부정점수={}",
                    result.getSentiment(),
                    result.getSentimentScore().getPositive(),
                    result.getSentimentScore().getNegative());
            return result;
        } catch (Exception e) {
            log.error("AWS Comprehend 호출 중 오류가 발생했습니다.", e);
            throw e;
        }
    }
}
