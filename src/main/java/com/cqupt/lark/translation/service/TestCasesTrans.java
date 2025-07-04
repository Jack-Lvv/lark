package com.cqupt.lark.translation.service;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;

import java.io.IOException;

public interface TestCasesTrans {

    String trans(String description, BrowserPageSupport browserPageSupport) throws IOException, InterruptedException;

    TestCase transToJson(String outMessageByAi) throws IOException;

    String transByVision(String aCase, BrowserPageSupport browserPageSupport) throws IOException, InterruptedException;

    TestCaseVision transToJsonWithVision(String standardCases);
}
