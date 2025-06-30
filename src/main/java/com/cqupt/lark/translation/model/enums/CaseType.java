package com.cqupt.lark.translation.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaseType {

    Click("点击元素"),
    Fill("填充文本"),
    ;

    private final String info;

}
