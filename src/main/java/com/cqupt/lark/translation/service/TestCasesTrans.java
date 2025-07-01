package com.cqupt.lark.translation.service;

import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Page;

import java.io.IOException;

public interface TestCasesTrans {

    String trans(String description, Page page) throws IOException;

    TestCase transToJson(String outMessageByAi) throws IOException;

}
