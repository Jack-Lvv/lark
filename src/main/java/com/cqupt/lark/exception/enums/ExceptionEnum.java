package com.cqupt.lark.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    BUSINESS_ERROR("业务异常"),
    SYSTEM_ERROR("系统异常"),
    PARAM_ERROR("参数异常"),
    NOT_FOUND("未找到"),
    UNAUTHORIZED("未授权"),
    FORBIDDEN("禁止访问"),
    NOT_SUPPORTED("不支持"),
    NOT_IMPLEMENTED("未实现"),
    ;

    private final String message;
}
