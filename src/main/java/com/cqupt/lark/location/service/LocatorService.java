package com.cqupt.lark.location.service;

import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public interface LocatorService {

    Locator getLocatorByJson(TestCase testCase, Page page);

}
