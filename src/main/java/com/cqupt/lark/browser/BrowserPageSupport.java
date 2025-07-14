package com.cqupt.lark.browser;

import com.cqupt.lark.common.PriorityLock;
import com.cqupt.lark.util.CompressImageUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;

import java.util.List;

public class BrowserPageSupport {

    private final Page pageInstance;

    private final PriorityLock lock;

    private final PlaywrightPoolManager playwrightPoolManager;

    public BrowserPageSupport(PlaywrightPoolManager playwrightPoolManager) throws InterruptedException {
        this.pageInstance = playwrightPoolManager.getPage();
        this.lock = playwrightPoolManager.getLock(pageInstance);
        this.playwrightPoolManager = playwrightPoolManager;
    }

    public void close() throws InterruptedException {
        lock.lockHighPriority();
        try {
            pageInstance.navigate("about:blank");
        } finally {
            lock.unlock();
        }
        playwrightPoolManager.releasePage(pageInstance);
    }

    public void waitForLoad() throws InterruptedException {
        lock.lockHighPriority();
        try {
            pageInstance.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(2000L));
        } finally {
            lock.unlock();
        }
    }

    public void navigate(String url) throws InterruptedException {
        lock.lockHighPriority();
        try {
            pageInstance.navigate(url);
        } finally {
            lock.unlock();
        }
    }

    public byte[] SSEScreenshot() throws InterruptedException {

        lock.lockLowPriority();
        try {
            return pageInstance.screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.JPEG)
                    .setQuality(80)
            );
        } finally {
            lock.unlock();
        }
    }

    public byte[] screenshot() throws InterruptedException {
        lock.lockHighPriority();
        try {
            byte[] screenshot = pageInstance.screenshot(new Page.ScreenshotOptions()
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
            pageInstance.mouse().click(x, y);
        } finally {
            lock.unlock();
        }
    }

    public void fill(int x, int y, String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            pageInstance.mouse().click(x, y);
            pageInstance.keyboard().type(text);
        } finally {
            lock.unlock();
        }
    }


    public void evaluate(String s, List<Integer> list) throws InterruptedException {
        lock.lockHighPriority();
        try {
            pageInstance.evaluate(s, list);
        } finally {
            lock.unlock();
        }
    }
    

    public String getContent() throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.content();
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
            return pageInstance.getByRole(role, new Page.GetByRoleOptions().setName(text));
        } finally {
            lock.unlock();
        }
    }

    public Locator getByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.getByText(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getByLocator(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.locator(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getLabelByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.getByLabel(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getPlaceholderByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.getByPlaceholder(text);
        } finally {
            lock.unlock();
        }
    }

    public Locator getTestIdByText(String text) throws InterruptedException {
        lock.lockHighPriority();
        try {
            return pageInstance.getByTestId(text);
        } finally {
            lock.unlock();
        }
    }


}
