package com.cqupt.lark.mock;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
public class MockTest {
    @Test
    public void testStorageState() {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType
                     .LaunchOptions()
                     .setHeadless(false)
                     .setChannel("msedge"))) {

            // 首次登录并保存状态
            BrowserContext context = browser.newContext(new Browser.NewContextOptions());
            Page page = context.newPage();
            page.navigate("https://aily.feishu.cn/ai/ailyplay/welcome");


            // 获取项目根目录路径（更可靠的方式）
            Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();
            // 保存状态到文件
            context.storageState(new BrowserContext.StorageStateOptions().setPath(resourcesPath));

        }
    }
    @Test
    public void testMock() {

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false))) {
            // 获取项目根目录路径（更可靠的方式）
            Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();
            // 之后可以使用保存的状态恢复登录
            BrowserContext newContext = browser.newContext(
                    new Browser.NewContextOptions().setStorageStatePath(resourcesPath)
            );
            Page newPage = newContext.newPage();
            newPage.navigate("https://aily.feishu.cn/ai/ailyplay");
            // 现在应该处于登录状态
            Thread.sleep(150000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
