````markdown
# MSA 기반 SurveyPulse 분석 보고서 서비스

SurveyPulse 플랫폼의 설문 응답을 바탕으로 AWS Comprehend로 감성 분석을 수행하고, 개별 및 전체 통계 보고서를 생성·조회하는 마이크로서비스입니다.

## 주요 기능

- **감성 분석 및 집계** (`POST /api/reports/analyze`)
  - AggregateRequest에 포함된 설문 ID, 응답 ID, 사용자 ID, 문항별 답변 목록을 받아
  - AWS Comprehend 호출을 통해 감성 지표(SentimentStats) 산출
  - `SentimentReport`(개별 감성 보고서) 저장 후
  - `OverallSentimentReport`(전체 감성 보고서) 생성 또는 갱신

- **질문별 감성 보고서 목록 조회** (`GET /api/reports/sentiments/{surveyId}/{questionId}?page={page}`)
  - 설문 ID와 질문 ID로 페이징된 개별 감성 보고서 목록 반환
  - SentimentReportDto 형태로 긍정·부정·중립·혼합 비율, 평균값 포함

- **단일 감성 보고서 조회** (`GET /api/reports/sentiment/{sentimentId}`)
  - `SentimentReportSingleDto` 형태로 단건 보고서 상세 정보 반환

- **설문별 전체 감성 보고서 요약 조회** (`GET /api/reports/overalls/{surveyId}?page={page}`)
  - `OverallSentimentReportSummaryDto` 형태로 질문별 전체 보고서 요약 반환

- **전체 감성 보고서 단건 조회** (`GET /api/reports/overall/{overallReportId}`)
  - `OverallSentimentReportDto` 형태로 전체 통계 및 질문 정보 포함 반환

## 기술 스펙

- **언어 & 프레임워크**: Java, Spring Boot
- **데이터베이스**: Spring Data JPA, MySQL (AWS RDS)
- **AWS 분석 연동**: AWS Comprehend
- **HTTP 클라이언트**: OpenFeign (Survey 서비스 호출)
- **회로 차단기 & 복원력**: Resilience4j
- **로깅 & 모니터링**: Elasticsearch, Logstash, Kibana (ELK), Prometheus, Grafana
- **보안**: Spring Security, JWT
- **CI/CD**: GitHub Actions
- **컨테이너 & 오케스트레이션**: Docker, Kubernetes, Helm, AWS EKS
- **아키텍처**: 마이크로서비스 아키텍처(MSA)

