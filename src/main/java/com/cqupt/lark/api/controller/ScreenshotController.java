package com.cqupt.lark.api.controller;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.browser.BrowserSession;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ScreenshotController {

    private final ExecutorService executor;

    private final BrowserSession browserSession;

    @Value("${app.config.sse-fps}")
    private int FPS;

    @GetMapping(value = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScreenshots() {

        SseEmitter emitter = new SseEmitter(0L);

        BrowserPageSupport browserPageSupport = browserSession.getBrowserPageSupport();

        executor.execute(() -> {
            log.info("SSE截图流连接建立");

            while (true) {
                long startTime = System.currentTimeMillis();

                // 截取页面截图
                byte[] screenshot;
                try {
                    screenshot = browserPageSupport.SSEScreenshot();
                } catch (InterruptedException e) {
                    throw new RuntimeException(ExceptionEnum.SSE_SCREENSHOT_ERROR.getMessage());
                }

                // 转换为Base64
                String base64Image = Base64.getEncoder().encodeToString(screenshot);

                // 发送SSE事件
                try {
                    emitter.send(SseEmitter.event()
                            .name("image")
                            .data(base64Image));
                } catch (IOException e) {
                    log.info("SSE发送截图失败,前端连接已断开");
                    break;
                }
                //log.info("发送截图成功");

                // 帧率控制
                long processingTime = System.currentTimeMillis() - startTime;
                long sleepTime = Math.max(0, 1000 / FPS - processingTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // 连接生命周期回调
        emitter.onCompletion(() -> {
            browserPageSupport.close();
            log.info("SSE截图连接断开");
        });
        emitter.onTimeout(() -> {
            browserPageSupport.close();
            log.info("SSE截图连接超时");
        });
        emitter.onError(e -> {
            browserPageSupport.close();
            log.error("SSE截图连接错误", e);
        });
        return emitter;
    }
}