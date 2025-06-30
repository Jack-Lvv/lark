package com.cqupt.lark.execute.service;

import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Page;

public interface Executor {

    Boolean execute(TestCase testCase, Page page);
}
