package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UITestController {

    private final TestCasesTrans testCasesTrans;
    private final TestExecutorService testExecutorService;
    private final ValidateService validateService;
    private final AssertService assertService;

    @Value("${app.config.max-retry-times}")
    private int maxFailureTimes;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    @GetMapping(value = "/api/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter test(RequestDTO request) {
        log.info("测试网址: {}", request.getUrl());
        log.info("测试用例描述: {}", request.getDescription());
        log.info("预期结果描述: {}", request.getExpectedResult());
        final String url;

        // 避免url报错
        if (!request.getUrl().startsWith("https://") && !request.getUrl().startsWith("http://")) {
            url = "https://" + request.getUrl();
        } else {
            url = request.getUrl();
        }

        SseEmitter emitter = new SseEmitter();

        executor.execute(() -> {
            BrowserPageSupport browserPageSupport = BrowserPageSupport.getInstance();

            try {
                browserPageSupport.navigate(url);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            String[] cases = SubStringUtils.subCasesStr(request.getDescription());
            int failureTimes = 0, index = 0;

            while (index < cases.length && failureTimes <= maxFailureTimes) {
                log.info("开始执行第{}个操作: {}", index + 1, cases[index]);
                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(Map.of("state", true,
                                    "text","开始执行操作：" + cases[index])));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                String standardStr;
                try {
                    standardStr = testCasesTrans.transByVision(cases[index], browserPageSupport);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                TestCaseVision testCaseVision;
                try {
                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
                    testCaseVision = testCasesTrans.transToJsonWithVision(standardCases);
                } catch (Throwable t) {
                    failureTimes++;
                    log.error("第{}个操作json转换失败: {}", index + 1, t.getMessage());
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(Map.of("state",false,
                                        "text","json数据转换失败，准备进行重试...")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                // 矫正大模型和页面的像素偏移量
                TestCaseVision testCaseVisionCorrected = OffsetCorrectUtils.correct(testCaseVision);

                TestResult testResult = new TestResult();
                //if (executor.execute(testCase, page)) {
                try {
                    if (testExecutorService.executeWithVision(testCaseVisionCorrected, browserPageSupport)) {
                        testResult = validateService.validate(browserPageSupport.screenshot(), cases[index]);
                    } else {
                        testResult.setStatus(false);
                        testResult.setDescription("准备进行重试...");
                    }
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }

                if (testResult.getStatus()) {
                    index++;
                    failureTimes = 0;
                    log.info("操作#{}成功...", index);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(Map.of("state", true,
                                        "text","操作执行成功，" + testResult.getDescription())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    failureTimes++;
                    log.info("测试#{}失败，进行重试...", index + 1);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(Map.of("state", false,
                                        "text","操作执行失败，" + testResult.getDescription())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // 重试次数满后，进行兜底定位操作
                if (failureTimes > maxFailureTimes) {
                    log.info("开始进行兜底定位...");
                    try {
                        emitter.send(SseEmitter.event()
                                .name("result")
                                .data(Map.of("state",true,
                                        "text","开始进行前端源码定位...")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        String standardCases = testCasesTrans.trans(cases[index], browserPageSupport);
                        String casesAfterCorrect = SubStringUtils.subCasesUselessPart(standardCases);
                        TestCase testCase = testCasesTrans.transToJson(casesAfterCorrect);
                        Boolean resultBoolean = testExecutorService.execute(testCase, browserPageSupport);
                        TestResult testResultByOCR = validateService.validate(browserPageSupport.screenshot(), cases[index]);
                        if (!resultBoolean) {
                            throw new Exception("源码定位失败");
                        } else if (!testResultByOCR.getStatus()) {
                            throw new Exception("操作执行失败，" + testResultByOCR.getDescription());
                        } else {
                            index++;
                            failureTimes = 0;
                            log.info("测试#{}成功...", index + 1);
                            emitter.send(SseEmitter.event()
                                    .name("result")
                                    .data(Map.of("state", true,
                                            "text", "操作执行成功，" + testResultByOCR.getDescription())));
                        }
                    } catch (Throwable t) {
                        log.info(t.getMessage());
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("result")
                                    .data(Map.of("state", false,
                                            "text", t.getMessage())));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            AssertResult assertResult = null;
            if (failureTimes > maxFailureTimes) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(Map.of("state", false,
                                    "text","执行失败，达到最大重试次数仍未执行成功")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (request.getExpectedResult() != null && !request.getExpectedResult().isEmpty()) {
                try {
                    assertResult = assertService.assertByVision(browserPageSupport.screenshot(), request.getExpectedResult());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (assertResult != null && assertResult.getStatus()) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(Map.of("state", true,
                                    "text","断言成功，" + assertResult.getDescription())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (assertResult != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(Map.of("state", false,
                                    "text","断言失败，" + assertResult.getDescription())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return emitter;

    }
}


