package com.cqupt.lark.browser.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BrowserPageSupport {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final ReentrantLock lock = new ReentrantLock(true);

    // 使用 volatile 关键字确保多线程环境下的可见性
    private static volatile BrowserPageSupport instance;
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
        if (isClosed) {
            return null;
        }
        lock.lock();
        if (isClosed) {
            return null;
        }
        try {
            return page.screenshot();
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

    public void close() {
        if (!isClosed) {
            lock.lock();
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

    public void evaluate(String s, List<Integer> list) {
        lock.lock();
        try {
            page.evaluate(s, list);
        } finally {
            lock.unlock();
        }
    }

    public String getContent() {
        lock.lock();
        try {
            return page.content();
        } finally {
            lock.unlock();
        }
    }

    public void locatorClick(Locator locator) {
        lock.lock();
        try {
            locator.click();
        } finally {
            lock.unlock();
        }
    }

    public void locatorFill(Locator locator, String locatorValue) {
        lock.lock();
        try {
            locator.fill(locatorValue);
        } finally {
            lock.unlock();
        }
    }

    public Locator getByRole(AriaRole role, String text) {
        lock.lock();
        try {
            return page.getByRole(role, new Page.GetByRoleOptions().setName(text));
        } finally {
            lock.unlock();
        }
    }

    public Locator getByText(String text) {
        lock.lock();
        try {
            return page.getByText(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getByLocator(String text) {
        lock.lock();
        try {
            return page.locator(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getLabelByText(String text) {
        lock.lock();
        try {
            return page.getByLabel(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getPlaceholderByText(String text) {
        lock.lock();
        try {
            return page.getByPlaceholder(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getTestIdByText(String text) {
        lock.lock();
        try {
            return page.getByTestId(text);
        } finally {
            lock.unlock();
        }
    }

}
