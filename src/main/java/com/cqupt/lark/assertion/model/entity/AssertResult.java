package com.cqupt.lark.assertion.model.entity;

import lombok.Data;

@Data
public class AssertResult {

    private Boolean status;

    private String description;

    public String toString() {
        if (status) {
            return "断言通过: " + description;
        } else {
            return "断言失败: " + description;
        }
    }
}
