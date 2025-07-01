package com.cqupt.lark;

import com.cqupt.lark.util.SubStringUtils;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

@SpringBootTest
@Slf4j
public class SubHtmlStringTest {

    @Test
    public void test() {

        try (Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false).setChannel("chrome").setSlowMo(1000));
            BrowserContext context = browser.newContext();
            Page page = context.newPage()) {
            page.navigate("https://aily.feishu.cn/ai/ailyplay/welcome");
            // 等待页面加载完成
            page.waitForLoadState(LoadState.NETWORKIDLE);
            log.info("当前页面标题: {}", page.title());
            String html = page.content();
            String cleanHtml = html.replaceAll("<image[^>]*xlink:href=\"data:image/[^;]+;base64,[^\"]+\"", "<image>")
                    .replaceAll("<path[^>]*>", "")              // 移除矢量图路径
                    .replaceAll("(?s)<script.*?</script>", "")  // 移除脚本
                    .replaceAll("<style.*?</style>", "")        // 移除样式
                    .replaceAll("\\s+", " ")                   // 压缩空白
                    .replaceAll("(?s)<!--.*?-->", "")       // 移除注释
                    .replaceAll("(?i)<meta[^>]*>", "");
            System.out.println(cleanHtml);
        }
    }
    @Test
    public void test2() {
        System.out.println(SubStringUtils.subCasesUselessPart("123sad{seTyp}asdf"));

    }
}
