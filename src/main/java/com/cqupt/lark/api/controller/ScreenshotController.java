package com.cqupt.lark.api.controller;

import com.cqupt.lark.browser.BrowserPageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {

            while (!browserPageSupport.getIsClosed()) {
                long startTime = System.currentTimeMillis();
                try {
                    // 截取页面截图
                    byte[] screenshot = browserPageSupport.SSEScreenshot();

                    if (screenshot == null) {
                        log.info("page已关闭");
                        continue;
                    }

                    // 转换为Base64
                    String base64Image = Base64.getEncoder().encodeToString(screenshot);

                    // 发送SSE事件
                    emitter.send(SseEmitter.event()
                            .name("image")
                            .data(base64Image));
                    //log.info("发送截图成功");

                    // 帧率控制
                    long processingTime = System.currentTimeMillis() - startTime;
                    long sleepTime = Math.max(0, 1000 / FPS - processingTime);
                    Thread.sleep(sleepTime);

                } catch (Exception e) {
                    emitter.completeWithError(e);
                    break;
                }
            }
        });
        return emitter;
    }
}