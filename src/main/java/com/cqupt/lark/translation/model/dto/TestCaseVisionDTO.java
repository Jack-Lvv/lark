package com.cqupt.lark.translation.model.dto;

import com.cqupt.lark.translation.model.enums.CaseType;
import lombok.Data;

@Data
public class TestCaseVisionDTO {

    private String caseType;

    private String caseValue;

    private int xUp;

    private int yUp;

    private int xDown;

    private int yDown;
}
