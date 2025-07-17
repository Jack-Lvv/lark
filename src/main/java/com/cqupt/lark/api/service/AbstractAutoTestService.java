package com.cqupt.lark.api.service;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.util.EmitterSendUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import com.cqupt.lark.vector.service.SearchVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAutoTestService implements AutoTestService {

    private final ValidateService validateService;
    private final Integer maxFailureTimes;
    private final SearchVectorService searchVectorService;

    @Override
    public boolean autoTest(String url, BrowserPageSupport browserPageSupport, SseEmitter emitter, String aCase) throws IOException {

        int failureTimes = 0;

        while (failureTimes <= maxFailureTimes) {
            log.info("开始执行操作: {}", aCase);
            try {
                EmitterSendUtils.send(emitter, "result", true, "开始执行操作：" + aCase);
            } catch (IOException e) {
                throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
            }

            String standardStr;

            try {
                standardStr = testTransByAi(aCase, browserPageSupport);
            } catch (IOException e) {
                throw new BusinessException(ExceptionEnum.PROMPT_ERROR);
            } catch (InterruptedException e) {
                throw new BusinessException(ExceptionEnum.SCREENSHOT_ERROR);
            }

            TestResult testResult = new TestResult();
            try {
                byte[] oldScreenshot = browserPageSupport.screenshot();
                if (executeTest(standardStr, browserPageSupport)) {
                    Thread.sleep(2000);
                    testResult = validateService.validate(oldScreenshot, browserPageSupport.screenshot(), aCase);
                } else {
                    testResult.setStatus(false);
                    testResult.setDescription("准备进行重试...");
                }
            } catch (IOException e) {
                throw new BusinessException(ExceptionEnum.PROMPT_ERROR);
            } catch (InterruptedException e) {
                throw new BusinessException(ExceptionEnum.SCREENSHOT_ERROR);
            } catch (BusinessException e) {
                EmitterSendUtils.send(emitter, "result", false, "json数据转换失败，准备进行重试...");
                failureTimes++;
                continue;
            }

            if (testResult.getStatus()) {
                log.info("操作成功...");
                try {
                    EmitterSendUtils.send(emitter, "result", true,
                            "操作执行成功，" + testResult.getDescription());
                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
                    searchVectorService.addQaRecord(url, aCase, standardCases, true);
                    return true;
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }
            } else {
                failureTimes++;
                log.info("测试失败，进行重试...");
                try {
                    EmitterSendUtils.send(emitter, "result", false,
                            "操作执行失败，" + testResult.getDescription());
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }
            }
        }
        return false;
    }

    public abstract boolean executeTest(String standardStr, BrowserPageSupport browserPageSupport) throws InterruptedException;

    public abstract String testTransByAi(String aCase, BrowserPageSupport browserPageSupport) throws IOException, InterruptedException;
}

