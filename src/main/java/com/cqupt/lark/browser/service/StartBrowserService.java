package com.cqupt.lark.browser.service;

import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class StartBrowserService {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final ReentrantLock lock = new ReentrantLock(true);

    // 使用 volatile 关键字确保多线程环境下的可见性
    private static volatile StartBrowserService instance;
    private volatile boolean isClosed = false;

    public void navigate(String url) {
        lock.lock();
        try {
            page.navigate(url);
        } finally {
            lock.unlock();
        }
    }

    public byte[] screenshot() {
        lock.lock();
        try {
            return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } finally {
            lock.unlock();
        }
    }

    public void click(int x, int y) {
        lock.lock();
        try {
            page.mouse().click(x, y);
        } finally {
            lock.unlock();
        }
    }

    public void fill(int x, int y, String text) {
        lock.lock();
        try {
            page.mouse().click(x, y);
            page.keyboard().type(text);
        } finally {
            lock.unlock();
        }
    }
    private StartBrowserService() {
        // 获取项目根目录路径
        Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setChannel("chrome"));
        context = browser.newContext(new Browser.NewContextOptions()
                .setStorageStatePath(resourcesPath));
        page = browser.newPage();
    }

    // 获取单例实例的静态方法
    public static StartBrowserService getInstance() {
        // 双重检查锁定
        if (instance == null) {
            synchronized (StartBrowserService.class) {
                if (instance == null) {
                    instance = new StartBrowserService();
                }
            }
        }
        return instance;
    }

    public Page getPage() {
        if (isClosed) {
            throw new IllegalStateException("StartBrowserService resources have been closed!");
        }
        return page;
    }
    public void close() {
        if (!isClosed) {
            try {
                if (page != null) page.close();
                if (context != null) context.close();
                if (browser != null) browser.close();
                if (playwright != null) playwright.close();
            } catch (Exception e) {
                System.err.println("Error closing Playwright resources: " + e.getMessage());
            } finally {
                isClosed = true; // 标记为已关闭
            }
        }
    }

    public void evaluate(String s, List<Integer> list) {
        lock.lock();
        try {
            page.evaluate(s, list);
        } finally {
            lock.unlock();
        }
    }

    public String getVideoUrl() {
        return page.video().path().toString();
    }
}
