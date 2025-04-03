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
            log.info("AWS Comprehend client built with static credentials");
        } else {
            this.comprehendClient = AmazonComprehendClientBuilder.standard()
                                                                 .withRegion(region)
                                                                 .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                                                                 .build();
            log.info("AWS Comprehend client built with default credentials provider chain");
        }
    }

    public DetectSentimentResult analyzeText(String text) {
        try {
            DetectSentimentRequest request = new DetectSentimentRequest()
                    .withText(text)
                    .withLanguageCode("ko");
            DetectSentimentResult result = comprehendClient.detectSentiment(request);
            log.info("AWS Comprehend result: {}", result.getSentiment());
            return result;
        } catch (Exception e) {
            log.error("Error calling AWS Comprehend", e);
            throw e;
        }
    }
}
