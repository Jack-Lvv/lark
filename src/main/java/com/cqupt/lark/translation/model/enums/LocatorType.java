package com.cqupt.lark.translation.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocatorType {

    Button("按钮"),
    TextBox("文本框"),
    Dialog("对话框"),
    Navigation("导航"),
    Switch("开关"),
    Label("标签"),
    Locator("定位器"),
    Placeholder("占位符"),
    Text("文本"),
    TestId("测试ID"),

    ;

    private final String info;
}
