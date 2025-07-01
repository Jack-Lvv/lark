package com.cqupt.lark.execute.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    private Boolean status;

    private String description;

    public String toString() {
        if (status) {
            return "测试通过: " + description;
        } else {
            return "测试失败: " + description;
        }
    }
}
