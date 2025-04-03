package com.example.report_service.exception;

import com.example.global.exception.ExceptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportExceptionType implements ExceptionType {
    REPORT_NOT_FOUND(6201, "해당 분석 데이터를 찾을 수 없습니다."),
    REPORT_SAVE_ERROR(6202, "분석 데이터 저장 중 오류가 발생했습니다."),
    INVALID_REPORT_DATA(6203, "분석 데이터가 올바르지 않습니다."),
    TEXTS_IS_EMPTY(6204, "분석할 텍스트 목록이 비어있습니다."),
    OVERALL_SENTIMENT_IS_EMPTY(6205, "전체 평균 통계 데이터가 존재하지 않습니다."),
    OVERALL_REPORT_NOT_FOUND(6206, "해당 평균 통계 데이터를 찾을 수 없습니다.");

    private final int statusCode;
    private final String message;
}
