package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.api.service.AssertTestResultService;
import com.cqupt.lark.api.service.impl.CodeAutoTestService;
import com.cqupt.lark.api.service.impl.VisionAutoTestService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.browser.BrowserSession;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.util.EmitterSendUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.util.UrlStringAdder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class NewUITestController {

    private final BrowserSession browserSession;

    private final VisionAutoTestService visionAutoTestService;
    private final CodeAutoTestService codeAutoTestService;
    private final AssertTestResultService assertTestResultService;

    private final ExecutorService executor;
    @GetMapping(value = "/api/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

            boolean isSuccess = false;
            for (String aCase : cases) {
                try {
                    isSuccess = visionAutoTestService.autoTest(browserPageSupport, emitter, aCase);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // 重试次数满后，进行兜底定位操作
                if (!isSuccess) {
                    log.info("开始进行兜底定位...");
                    try {
                        EmitterSendUtils.send(emitter, "result",
                                false, "执行失败，准备进行前端代码定位");
                    } catch (IOException e) {
                        throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
                    }
                    try {
                        isSuccess = codeAutoTestService.autoTest(browserPageSupport, emitter, aCase);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!isSuccess) {
                    break;
                }
            }

            if (isSuccess) {
                assertTestResultService.assertResult(emitter, request, browserPageSupport);
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


