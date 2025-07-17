package com.cqupt.lark.browser;

import com.cqupt.lark.common.PriorityLock;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class PlaywrightPoolManager {

    private static final Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();

    static class PlaywrightWrapper {

        final Playwright pwt;
        final PriorityLock lock = new PriorityLock();
        final AtomicInteger size = new AtomicInteger(0);

        PlaywrightWrapper(Playwright pwt) {
            this.pwt = pwt;
        }

        PlaywrightWrapper(Playwright pwt, int size) {
            this.pwt = pwt;
            this.size.set(size);
        }
    }

    private static final ConcurrentSkipListSet<PlaywrightWrapper> playwrightQueue =
            new ConcurrentSkipListSet<>(Comparator.comparingInt(o -> o.size.get()));

    private static final ConcurrentLinkedQueue<Page> unUsedPages = new ConcurrentLinkedQueue<>();

    private static final Map<Page, PlaywrightWrapper> pagePlaywrightMap = new ConcurrentHashMap<>();

    public PlaywrightPoolManager() {
        Thread cleaner = new Thread(this::cleanupLoop);
        cleaner.setDaemon(true);
        cleaner.start();
    }

    @PreDestroy
    public void shoutDown() {
        for (PlaywrightWrapper wrapper : playwrightQueue) {
            wrapper.pwt.close();
        }
    }

    public PriorityLock getLock(Page page) {
        return pagePlaywrightMap.get(page).lock;
    }

    public Page getPage() throws InterruptedException {
        Page page = unUsedPages.poll();
        if (page != null) {
            return page;
        }

        PlaywrightWrapper wrapper;
        if (playwrightQueue.isEmpty() || playwrightQueue.first().size.get() >= 10) {
            wrapper = new PlaywrightWrapper(Playwright.create());
        } else {
            // 处理 floor() 返回 null 的情况
            wrapper = playwrightQueue.floor(new PlaywrightWrapper(null, 9));
            if (wrapper == null) {
                wrapper = new PlaywrightWrapper(Playwright.create());
            } else {
                // 正确更新集合：先移除再修改
                playwrightQueue.remove(wrapper);
            }
        }
        Page newPage;
        wrapper.lock.lockHighPriority();
        try {
            Browser browser = wrapper.pwt.chromium().launch();
            BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
                    .setStorageStatePath(resourcesPath)
            );
            newPage = ctx.newPage();
        } finally {
            wrapper.lock.unlock();
        }
        wrapper.size.incrementAndGet();
        playwrightQueue.add(wrapper);
        pagePlaywrightMap.put(newPage, wrapper);
        return newPage;


    }

    public void releasePage(Page page) {
        PlaywrightWrapper wrapper = pagePlaywrightMap.get(page);
        if (wrapper == null) {
            return;
        }
        unUsedPages.offer(page);
        playwrightQueue.remove(wrapper);
        wrapper.size.decrementAndGet();
        playwrightQueue.add(wrapper);

    }


    private void cleanupLoop() {
        while (true) {
            try {
                Thread.sleep(5 * 60 * 100); // 每5分钟检查一次
                if (playwrightQueue.size() > 1) {
                    cleanupPages();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void cleanupPages() throws InterruptedException {

        Iterator<PlaywrightWrapper> iterator = playwrightQueue.iterator();
        while (iterator.hasNext()) {
            PlaywrightWrapper wrapper = iterator.next();
            if (wrapper.size.get() == 0) {
                unUsedPages.removeIf(page -> {
                    PlaywrightWrapper pw = pagePlaywrightMap.get(page);
                    return pw == wrapper;
                });
                wrapper.lock.lockHighPriority();
                try {
                    wrapper.pwt.close();
                } finally {
                    wrapper.lock.unlock();
                }
                iterator.remove();
            }
        }

    }

}


