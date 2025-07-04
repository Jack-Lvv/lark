package com.cqupt.lark.browser;

import com.cqupt.lark.common.PriorityLock;
import com.cqupt.lark.util.CompressImageUtils;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ScreenshotType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BrowserPageSupport {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final PriorityLock lock = new PriorityLock();

    // 使用 volatile 关键字确保多线程环境下的可见性
    private static volatile BrowserPageSupport instance;
    private volatile boolean isClosed = false;

    public void navigate(String url) throws InterruptedException {
        lock.lockHighPriority();
        try {
            page.navigate(url);
        } finally {
            lock.unlock();
        }
    }

    public byte[] SSEScreenshot() throws InterruptedException {
        if (isClosed) {
            return null;
        }
        lock.lockLowPriority();
        if (isClosed) {
            return null;
        }
        try {
            return page.screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.JPEG)
                    .setQuality(80)
                    .setFullPage(true));
        } finally {
            lock.unlock();
        }
    }

    public byte[] screenshot() throws InterruptedException {
        lock.lockHighPriority();
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.JPEG)
                    .setQuality(80)
                    .setFullPage(true));
            return CompressImageUtils.compressImage(screenshot, 640, 360);
        } catch (Exception e) {
            throw new RuntimeException( "图片压缩失败", e);
        } finally {
            lock.unlock();
        }
    }

    public void click(int x, int y) throws InterruptedException {
        lock.lockHighPriority();
        try {
            page.mouse().click(x, y);
        } finally {
            lock.unlock();
        }
    }

    public void fill(int x, int y, String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            page.mouse().click(x, y);
            page.keyboard().type(text);
        } finally {
            lock.unlock();
        }
    }

    private BrowserPageSupport() {
        // 获取项目根目录路径
        Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true)
                //.setChannel("chrome")
        );
        context = browser.newContext(new Browser.NewContextOptions()
                .setStorageStatePath(resourcesPath));
        page = browser.newPage();
    }

    // 获取单例实例的静态方法
    public static BrowserPageSupport getInstance() {
        // 双重检查锁定
        if (instance == null) {
            synchronized (BrowserPageSupport.class) {
                if (instance == null) {
                    instance = new BrowserPageSupport();
                }
            }
        }
        return instance;
    }

    public boolean getIsClosed() {
        return isClosed;
    }

    public void close() throws InterruptedException {
        if (!isClosed) {
            lock.lockHighPriority();
            try {
                if (page != null) page.close();
                if (context != null) context.close();
                if (browser != null) browser.close();
                if (playwright != null) playwright.close();
            } finally {
                isClosed = true;
                lock.unlock();
            }
        }
    }

    public void evaluate(String s, List<Integer> list) throws InterruptedException {
        lock.lockHighPriority();
        try {
            page.evaluate(s, list);
        } finally {
            lock.unlock();
        }
    }

    public String getContent() throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.content();
        } finally {
            lock.unlock();
        }
    }

    public void locatorClick(Locator locator) throws InterruptedException {
        lock.lockHighPriority();
        try {
            locator.click();
        } finally {
            lock.unlock();
        }
    }

    public void locatorFill(Locator locator, String locatorValue) throws InterruptedException {
        lock.lockHighPriority();
        try {
            locator.fill(locatorValue);
        } finally {
            lock.unlock();
        }
    }

    public Locator getByRole(AriaRole role, String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.getByRole(role, new Page.GetByRoleOptions().setName(text));
        } finally {
            lock.unlock();
        }
    }

    public Locator getByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.getByText(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getByLocator(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.locator(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getLabelByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.getByLabel(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getPlaceholderByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.getByPlaceholder(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getTestIdByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return page.getByTestId(text);
        } finally {
            lock.unlock();
        }
    }

}
