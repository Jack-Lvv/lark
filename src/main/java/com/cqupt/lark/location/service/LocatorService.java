package com.cqupt.lark.location.service;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Locator;

public interface LocatorService {

    Locator getLocatorByJson(TestCase testCase, BrowserPageSupport browserPageSupport) throws InterruptedException;

}
