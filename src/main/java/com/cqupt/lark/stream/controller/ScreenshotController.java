package com.cqupt.lark.stream.controller;

import com.cqupt.lark.browser.service.StartBrowserService;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    StartBrowserService startBrowserService = StartBrowserService.getInstance();

    @GetMapping(value = "/screenshots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamScreenshots() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {

            Page page = startBrowserService.getPage();

            while (!page.isClosed()) {
                try {
                    // 截取页面截图
                    byte[] screenshot = startBrowserService.screenshot();

                    // 转换为Base64
                    String base64Image = Base64.getEncoder().encodeToString(screenshot);

                    // 发送SSE事件
                    emitter.send(SseEmitter.event()
                            .name("image")
                            .data(base64Image));
                    log.info("发送截图成功");

                    Thread.sleep(100);
                } catch (Exception e) {
                    emitter.completeWithError(e);
                    break;
                }
            }
        });
        return emitter;
    }
}