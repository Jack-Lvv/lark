package com.cqupt.lark.execute.service.impl;

import com.cqupt.lark.execute.service.Executor;
import com.cqupt.lark.location.service.LocatorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutorImpl implements Executor {

    private final LocatorService locatorService;

    @Override
    public Boolean execute(TestCase testCase, Page page) {
        Locator locator = locatorService.getLocatorByJson(testCase, page);
        if (locator == null) {
            return false;
        }
        try {
            switch (testCase.getCaseType()) {
                case Click:
                    locator.waitFor();
                    locator.click();
                    break;
                case Fill:
                    locator.waitFor();
                    locator.fill(testCase.getLocatorValue());
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
