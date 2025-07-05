package com.cqupt.lark.exception;

import com.cqupt.lark.exception.enums.ExceptionEnum;

public class BusinessException extends RuntimeException{
    public BusinessException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
    }
}
