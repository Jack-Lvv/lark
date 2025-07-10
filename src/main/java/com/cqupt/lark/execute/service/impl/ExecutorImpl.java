package com.cqupt.lark.execute.service.impl;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.location.service.LocatorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.util.EmitterSendUtils;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.validate.service.ValidateService;
import com.cqupt.lark.vector.model.dto.AnswerDTO;
import com.cqupt.lark.vector.service.SearchVectorService;
import com.microsoft.playwright.Locator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.cqupt.lark.translation.service.TestCasesTrans;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutorImpl implements TestExecutorService {

    private final LocatorService locatorService;
    private final TestCasesTrans testCasesTrans;
    private final ValidateService validateService;
    private final SearchVectorService searchVectorService;

    @Override
    public Boolean execute(TestCase testCase, BrowserPageSupport browserPageSupport) throws InterruptedException {
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
    public boolean executeWithVision(TestCaseVision testCaseVision, BrowserPageSupport browserPageSupport) throws InterruptedException {
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
                "  window._highlightDiv = div;\n" +  // 保存引用
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

    @Override
    public boolean executeWithAnswer(AnswerDTO answer, BrowserPageSupport browserPageSupport, String aCase,
                                     SseEmitter emitter) throws InterruptedException, IOException {
        TestCaseVision testCaseVision = testCasesTrans.transToJsonWithVision(answer.getAnswer());
        TestCaseVision testCaseVisionCorrected = OffsetCorrectUtils.correct(testCaseVision);
        byte[] oldScreenshot = browserPageSupport.screenshot();
        TestResult testResult = new TestResult();
        if (executeWithVision(testCaseVisionCorrected, browserPageSupport)) {
            browserPageSupport.waitForLoad();
            testResult = validateService.validate(oldScreenshot, browserPageSupport.screenshot(), aCase);
        }
        if (!testResult.getStatus()) {
            searchVectorService.wrongQaRecord(answer.getVectorId());
            return false;
        } else {
            EmitterSendUtils.send(emitter, "result", true,
                    "操作执行成功，" + testResult.getDescription());
            return true;
        }
    }
}
