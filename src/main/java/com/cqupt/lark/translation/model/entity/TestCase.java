package com.cqupt.lark.translation.model.entity;

import com.cqupt.lark.translation.model.enums.CaseType;
import com.cqupt.lark.translation.model.enums.LocatorType;
import com.microsoft.playwright.Locator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCase {

    private CaseType caseType;

    private LocatorType locatorType;

    private String caseValue;

    private String locatorValue;
}
