package com.cqupt.lark.execute.service.impl;

import com.cqupt.lark.execute.service.Executor;
import com.cqupt.lark.location.service.LocatorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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

    @Override
    public boolean executeWithVision(TestCaseVision testCaseVision, Page page) {
        int x1 = testCaseVision.getXUp();
        int y1 = testCaseVision.getYUp();
        int x2 = testCaseVision.getXDown();
        int y2 = testCaseVision.getYDown();
        int width = x2 - x1;
        int height = y2 - y1;

        // 注入CSS样式来创建红色边框
        page.evaluate("([x, y, w, h]) => {\n" +
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
                    page.mouse().click(midX, midY);
                    break;
                case Fill:
                    page.mouse().click(midX, midY);
                    page.keyboard().type(testCaseVision.getCaseValue());
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
