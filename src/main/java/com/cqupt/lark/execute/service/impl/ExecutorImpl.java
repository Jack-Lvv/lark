package com.cqupt.lark.execute.service.impl;

import com.cqupt.lark.browser.service.BrowserPageSupport;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.location.service.LocatorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.microsoft.playwright.Locator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutorImpl implements TestExecutorService {

    private final LocatorService locatorService;

    @Override
    public Boolean execute(TestCase testCase, BrowserPageSupport browserPageSupport) {
        Locator locator = locatorService.getLocatorByJson(testCase, browserPageSupport);
        if (locator == null) {
            return false;
        }
        try {
            switch (testCase.getCaseType()) {
                case Click:
                    browserPageSupport.locatorClick(locator);
                    break;
                case Fill:
                    browserPageSupport.locatorFill(locator, testCase.getLocatorValue());
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean executeWithVision(TestCaseVision testCaseVision, BrowserPageSupport browserPageSupport) {
        int x1 = testCaseVision.getXUp();
        int y1 = testCaseVision.getYUp();
        int x2 = testCaseVision.getXDown();
        int y2 = testCaseVision.getYDown();
        int width = x2 - x1;
        int height = y2 - y1;

        // 注入CSS样式来创建红色边框
        browserPageSupport.evaluate("([x, y, w, h]) => {\n" +
                "  const div = document.createElement('div');\n" +
                "  div.style.position = 'absolute';\n" +
                "  div.style.left = x + 'px';\n" +
                "  div.style.top = y + 'px';\n" +
                "  div.style.width = w + 'px';\n" +
                "  div.style.height = h + 'px';\n" +
                "  div.style.border = '2px solid red';\n" +
                "  div.style.zIndex = '9999';\n" +
                "  div.style.pointerEvents = 'none';\n" +
                "  document.body.appendChild(div);\n" +
                "}", Arrays.asList(x1, y1, width, height));


        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        try {
            switch (testCaseVision.getCaseType()) {
                case Click:
                    browserPageSupport.click(midX, midY);
                    break;
                case Fill:
                    browserPageSupport.fill(midX, midY, testCaseVision.getCaseValue());
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
