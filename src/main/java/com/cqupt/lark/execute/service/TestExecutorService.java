package com.cqupt.lark.execute.service;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;

public interface TestExecutorService {

    Boolean execute(TestCase testCase, BrowserPageSupport browserPageSupport);

    boolean executeWithVision(TestCaseVision testCaseVision, BrowserPageSupport browserPageSupport);
}
