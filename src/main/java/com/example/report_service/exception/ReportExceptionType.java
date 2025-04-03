package com.example.report_service.exception;

import com.example.global.exception.ExceptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportExceptionType implements ExceptionType {

    private final int statusCode;
    private final String message;
}
