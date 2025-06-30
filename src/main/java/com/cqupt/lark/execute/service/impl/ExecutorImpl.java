package com.cqupt.lark.execute.service.impl;

import com.cqupt.lark.execute.service.Executor;
import com.cqupt.lark.location.repository.LocationRepository;
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

    private final LocationRepository locationRepository;

    @Override
    public Boolean execute(TestCase testCase, Page page) {
        Locator locator;
        switch (testCase.getCaseType()) {
            case Click:
                locator = locationRepository.getByLocator(testCase.getLocatorValue(), page);
                if (locator == null) {
                    return false;
                }
                locator.waitFor();
                locator.click();
                break;
            case Fill:
                locator = locationRepository.getByLocator(testCase.getLocatorValue(), page);
                if (locator == null) {
                    return false;
                }
                locator.waitFor();
                locator.fill(testCase.getLocatorValue());
                break;
        }

        return null;
    }
}
