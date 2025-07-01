package com.cqupt.lark;

import com.cqupt.lark.agent.service.Assistant;
import com.cqupt.lark.execute.repository.ActionRepository;
import com.cqupt.lark.location.repository.LocationRepository;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@SpringBootTest
public class ActionTest {
    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private Assistant assistant;
    @Autowired
    private LocationRepository locationRepository;

    @Test
    public void test() throws InterruptedException {

        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = playwright.chromium();
            try (Browser browser = browserType.launch(new BrowserType.LaunchOptions()
                    .setHeadless(false).setChannel("chrome").setSlowMo(1000))) {
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("src/main/resources/videos"))
                        .setRecordVideoSize(1280, 720)); // 设置视频尺寸);
                try (Page page = context.newPage()) {
                    page.navigate("https://aily.feishu.cn/ai/ailyplay/welcome");
                    // 等待页面加载完成
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    log.info("当前页面标题: {}", page.title());
                    byte[] imageBytes = page.screenshot();
                    Files.write(Paths.get("D:/Desktop/picture.png"), imageBytes);


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
