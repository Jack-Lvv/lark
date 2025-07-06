package com.cqupt.lark.api.controller;

import com.cqupt.lark.browser.BrowserPageSupport;
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
import java.util.concurrent.Executors;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ScreenshotController {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${app.config.sse-fps}")
    private int FPS;

    @GetMapping(value = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScreenshots() {

        BrowserPageSupport browserPageSupport = BrowserPageSupport.getInstance();

        SseEmitter emitter = new SseEmitter(60000L);

        executor.execute(() -> {

            while (!browserPageSupport.getIsClosed()) {
                long startTime = System.currentTimeMillis();

                // 截取页面截图
                byte[] screenshot;
                try {
                    screenshot = browserPageSupport.SSEScreenshot();
                } catch (InterruptedException e) {
                    throw new RuntimeException(ExceptionEnum.SSE_SCREENSHOT_ERROR.getMessage());
                }

                if (screenshot == null) {
                    log.info("page已关闭");
                    emitter.complete();
                    break;
                }

                // 转换为Base64
                String base64Image = Base64.getEncoder().encodeToString(screenshot);

                // 发送SSE事件
                try {
                    emitter.send(SseEmitter.event()
                            .name("image")
                            .data(base64Image));
                } catch (IOException e) {
                    throw new RuntimeException(ExceptionEnum.SSE_SCREENSHOT_SEND_ERROR.getMessage());
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
        return emitter;
    }
}