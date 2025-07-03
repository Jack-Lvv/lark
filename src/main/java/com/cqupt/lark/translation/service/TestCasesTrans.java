package com.cqupt.lark.translation.service;

import com.cqupt.lark.browser.service.StartBrowserService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.microsoft.playwright.Page;

import java.io.IOException;

public interface TestCasesTrans {

    String trans(String description, Page page) throws IOException;

    TestCase transToJson(String outMessageByAi) throws IOException;

    String transByVision(String aCase, StartBrowserService startBrowserService) throws IOException;

    TestCaseVision transToJsonWithVision(String standardCases);
}
