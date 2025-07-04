package com.cqupt.lark;

import com.cqupt.lark.util.CompressImageUtils;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;

@SpringBootTest
public class CompressTest {
    @Test
    public void test() throws Exception {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch();
        BrowserContext browserContext = browser.newContext();
        Page page = browserContext.newPage();
        page.navigate("https://aily.feishu.cn/ai/ailyplay");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                .setType(ScreenshotType.JPEG)
                .setQuality(80)
        );
        byte[] bytes = CompressImageUtils.compressImage(screenshot, 640, 360);

        File outputFile = new File("D:/Desktop/QA/lark/src/main/resources/static/1.png");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(bytes);
        }

        // 80kB(100%) -> 42kB(90%) -> 32kB(80%) -> 28kB(70%) -> 23kB(50%)
        browser.close();
        playwright.close();
    }

}
