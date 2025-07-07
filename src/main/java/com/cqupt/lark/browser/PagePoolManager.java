package com.cqupt.lark.browser;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class PagePoolManager {

    private final Playwright playwright;
    private final Browser browser;

    private static final Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();

    @Data
    @AllArgsConstructor
    static class ContextWithSize {
        private BrowserContext browserContext;
        private int size;
    }

    private static final ConcurrentSkipListSet<ContextWithSize> browserContextMap =
            new ConcurrentSkipListSet<>(Comparator.comparingInt(o -> o.size));

    private static final ConcurrentLinkedQueue<Page> unUsedPageSet = new ConcurrentLinkedQueue<>();

    private static final Map<Page, ContextWithSize> contextPageSizeMap = new ConcurrentHashMap<>();

    public PagePoolManager() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true).setSlowMo(1000));
        startCleanupThread();
    }

    @PreDestroy
    public void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private Page createPage(ContextWithSize contextWithSize) {
        Page page = contextWithSize.browserContext.newPage();
        contextPageSizeMap.put(page, contextWithSize);
        return page;
    }
    public Page getPage() {
        Page page = unUsedPageSet.poll();
        if (page != null) {
            return page;
        } else if (browserContextMap.isEmpty() || browserContextMap.first().size > 9) {
            BrowserContext browserContext;
            synchronized (this) {
                browserContext = browser.newContext(new Browser.NewContextOptions()
                        .setStorageStatePath(resourcesPath));
            }
            ContextWithSize contextWithSize = new ContextWithSize(browserContext, 1);
            browserContextMap.add(contextWithSize);
            return createPage(contextWithSize);
        } else {
            ContextWithSize contextWithSize = browserContextMap.headSet(new ContextWithSize(null, 10)).last();
            contextWithSize.size++;
            return createPage(contextWithSize);
        }
    }

    public void releasePage(Page page) {
        // page 的数据清理部分
        page.navigate("about:blank");
        unUsedPageSet.add(page);
        ContextWithSize contextWithSize = contextPageSizeMap.get(page);
        browserContextMap.remove(contextWithSize);
        contextWithSize.size--;
        browserContextMap.add(contextWithSize);
    }


    private void startCleanupThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(300000); // 每5分钟检查一次
                    cleanupIdlePages();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void cleanupIdlePages() {
        ContextWithSize contextWithSize = browserContextMap.pollFirst();
        if (contextWithSize != null && contextWithSize.size == 0) {
            contextWithSize.browserContext.close();
        } else if (contextWithSize != null && contextWithSize.size > 0) {
            browserContextMap.add(contextWithSize);
        }
    }

}
