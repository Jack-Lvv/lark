package com.cqupt.lark.translation.model.entity;

import com.cqupt.lark.translation.model.enums.CaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseVision {

    private CaseType caseType;

    private String caseValue;

    private int xUp;

    private int yUp;

    private int xDown;

    private int yDown;

}
