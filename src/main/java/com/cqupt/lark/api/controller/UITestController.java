package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.browser.BrowserSession;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.EmitterSendUtils;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.util.UrlStringAdder;
import com.cqupt.lark.validate.service.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UITestController {

    private final TestCasesTrans testCasesTrans;
    private final TestExecutorService testExecutorService;
    private final ValidateService validateService;
    private final AssertService assertService;
    private final BrowserSession browserSession;

    @Value("${app.config.vision-max-retry-times}")
    private int maxFailureTimes;

    private final ExecutorService executor;
    @GetMapping(value = "/api/test/v2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter test(RequestDTO request) {
        log.info("测试网址: {}", request.getUrl());
        log.info("测试用例描述: {}", request.getDescription());
        log.info("预期结果描述: {}", request.getExpectedResult());

        BrowserPageSupport browserPageSupport = browserSession.getBrowserPageSupport();

        SseEmitter emitter = new SseEmitter(0L);

        executor.execute(() -> {

            try {
                browserPageSupport.navigate(UrlStringAdder.urlStrAdd(request.getUrl()));
                browserPageSupport.waitForLoad();
            } catch (InterruptedException e) {
                throw new BusinessException(ExceptionEnum.NAVIGATE_ERROR);
            }


            String[] cases = SubStringUtils.subCasesStr(request.getDescription());
            int failureTimes = 0, index = 0;

            while (index < cases.length && failureTimes <= maxFailureTimes) {
                log.info("开始执行第{}个操作: {}", index + 1, cases[index]);
                try {
                    EmitterSendUtils.send(emitter, "result", true, "开始执行操作：" + cases[index]);
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }

                String standardStr;
                try {
                    standardStr = testCasesTrans.transByVision(cases[index], browserPageSupport);
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.PROMPT_ERROR);
                } catch (InterruptedException e) {
                    throw new BusinessException(ExceptionEnum.SCREENSHOT_ERROR);
                }

                TestCaseVision testCaseVision;
                try {
                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
                    testCaseVision = testCasesTrans.transToJsonWithVision(standardCases);
                } catch (Throwable t) {
                    failureTimes++;
                    log.error("第{}个操作json转换失败: {}", index + 1, t.getMessage());
                    try {
                        EmitterSendUtils.send(emitter, "result", false, "json数据转换失败，准备进行重试...");
                    } catch (IOException e) {
                        throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                    }
                    continue;
                }

                // 矫正大模型和页面的像素偏移量
                TestCaseVision testCaseVisionCorrected = OffsetCorrectUtils.correct(testCaseVision);

                TestResult testResult = new TestResult();
                //if (executor.execute(testCase, page)) {
                try {
                    byte[] oldScreenshot = browserPageSupport.screenshot();
                    if (testExecutorService.executeWithVision(testCaseVisionCorrected, browserPageSupport)) {
                        browserPageSupport.waitForLoad();
                        testResult = validateService.validate(oldScreenshot, browserPageSupport.screenshot(), cases[index]);
                    } else {
                        testResult.setStatus(false);
                        testResult.setDescription("准备进行重试...");
                    }
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.PROMPT_ERROR);
                } catch (InterruptedException e) {
                    throw new BusinessException(ExceptionEnum.SCREENSHOT_ERROR);
                }

                if (testResult.getStatus()) {
                    index++;
                    failureTimes = 0;
                    log.info("操作#{}成功...", index);
                    try {
                        EmitterSendUtils.send(emitter, "result", true,
                                "操作执行成功，" + testResult.getDescription());
                    } catch (IOException e) {
                        throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                    }
                } else {
                    failureTimes++;
                    log.info("测试#{}失败，进行重试...", index + 1);
                    try {
                        EmitterSendUtils.send(emitter, "result", false,
                                "操作执行失败，" + testResult.getDescription());
                    } catch (IOException e) {
                        throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                    }
                }

                // 重试次数满后，进行兜底定位操作
                if (failureTimes > maxFailureTimes) {
                    log.info("开始进行兜底定位...");
                    try {
                        EmitterSendUtils.send(emitter, "result", true, "开始进行前端源码定位...");
                    } catch (IOException e) {
                        throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                    }
                    try {
                        String standardCases = testCasesTrans.trans(cases[index], browserPageSupport);
                        String casesAfterCorrect = SubStringUtils.subCasesUselessPart(standardCases);
                        TestCase testCase = testCasesTrans.transToJson(casesAfterCorrect);
                        byte[] oldScreenshot = browserPageSupport.screenshot();
                        Boolean resultBoolean = testExecutorService.execute(testCase, browserPageSupport);
                        browserPageSupport.waitForLoad();
                        TestResult testResultByOCR = validateService.validate(oldScreenshot, browserPageSupport.screenshot(), cases[index]);
                        if (!resultBoolean) {
                            throw new Exception("源码定位失败");
                        } else if (!testResultByOCR.getStatus()) {
                            throw new Exception("操作执行失败，" + testResultByOCR.getDescription());
                        } else {
                            index++;
                            failureTimes = 0;
                            log.info("测试#{}成功...", index + 1);
                            EmitterSendUtils.send(emitter, "result",
                                    true, "操作执行成功，" + testResultByOCR.getDescription());

                        }
                    } catch (Throwable t) {
                        log.info(t.getMessage());
                        throw new BusinessException(ExceptionEnum.CODE_LOCATION_ERROR);
                    }
                }
            }

            AssertResult assertResult = null;
            if (failureTimes > maxFailureTimes) {
                try {
                    EmitterSendUtils.send(emitter, "result",
                            false, "执行失败，达到最大重试次数仍未执行成功");
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }
            } else if (request.getExpectedResult() != null && !request.getExpectedResult().isEmpty()) {
                try {
                    assertResult = assertService.assertByVision(browserPageSupport.screenshot(), request.getExpectedResult());
                } catch (IOException | InterruptedException e) {
                    throw new BusinessException(ExceptionEnum.ASSERT_ERROR);
                }
            }

            if (assertResult != null && assertResult.getStatus()) {
                try {
                    EmitterSendUtils.send(emitter, "result",
                            true, "断言成功，" + assertResult.getDescription());
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }
            } else if (assertResult != null) {
                try {
                    EmitterSendUtils.send(emitter, "result",
                            false, "断言失败，" + assertResult.getDescription());
                } catch (IOException e) {
                    throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                }
            }
        });
        // 连接生命周期回调
        emitter.onCompletion(() -> {
            log.info("SSE命令连接断开");
        });
        emitter.onTimeout(() -> {
            log.info("SSE命令连接超时");
        });
        emitter.onError(e -> {
            log.error("SSE命令连接错误", e);
        });

        return emitter;

    }
}


