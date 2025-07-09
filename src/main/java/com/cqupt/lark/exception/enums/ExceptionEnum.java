package com.cqupt.lark.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnum {

    SSE_SCREENSHOT_ERROR("SSE流的屏幕截图失败"),
    SSE_SCREENSHOT_SEND_ERROR("SSE流的屏幕截图发送失败"),
    NAVIGATE_ERROR("页面导航失败，请检查你输入的网址"),
    SSE_SEND_ERROR("SSE流发送失败，请联系管理员修复"),
    PROMPT_ERROR("后台系统提示词失效，请联系管理员修复"),
    SCREENSHOT_ERROR("页面图片获取失败，请联系管理员修复"),
    CODE_LOCATION_ERROR("前端原代码定位失败"),
    ASSERT_ERROR("页面断言失败"),
    JSON_TRANS_ERROR("JSON转换失败"),

    ;

    private final String message;
}
