package com.cqupt.lark.browser;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PagePoolManager {

    private final Playwright playwright;
    private final Browser browser;

    private static final Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();



    static class ContextWrapper {

        final BrowserContext ctx;
        final AtomicInteger size = new AtomicInteger(0);

        ContextWrapper(BrowserContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ContextWrapper
                    && ((ContextWrapper) o).ctx == this.ctx;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(ctx);
        }
    }

    private static final PriorityQueue<ContextWrapper> contextQueue =
            new PriorityQueue<>(Comparator.comparingInt(o -> o.size.get()));

    private final ReentrantLock poolLock = new ReentrantLock();

    private static final ConcurrentLinkedQueue<Page> unUsedPages = new ConcurrentLinkedQueue<>();

    private static final Map<Page, ContextWrapper> pageContextMap = new ConcurrentHashMap<>();

    public PagePoolManager() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true).setSlowMo(1000));
        Thread cleaner = new Thread(this::cleanupLoop);
        cleaner.setDaemon(true);
        cleaner.start();
    }


    @PreDestroy
    public void closeBrowser() {
        browser.close();
        playwright.close();
    }

    public Page getPage() {
        Page page = unUsedPages.poll();
        if (page != null) {
            return page;
        }
        poolLock.lock();
        page = unUsedPages.poll();
        if (page != null) {
            return page;
        }
        try {
            ContextWrapper wrapper;
            if (contextQueue.isEmpty() || contextQueue.peek().size.get() >= 10) {
                BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
                        .setStorageStatePath(resourcesPath));
                wrapper = new ContextWrapper(ctx);
            } else {
                wrapper = contextQueue.poll();
            }
            Page newPage = wrapper.ctx.newPage();
            wrapper.size.incrementAndGet();
            contextQueue.add(wrapper);
            pageContextMap.put(newPage, wrapper);
            return newPage;
        } finally {
            poolLock.unlock();
        }

    }

    public void releasePage(Page page) {
        // page 的数据清理部分
        page.navigate("about:blank");
        unUsedPages.offer(page);
        ContextWrapper wrapper = pageContextMap.remove(page);
        if (wrapper == null) {
            return;
        }
        poolLock.lock();
        try {
            contextQueue.remove(wrapper);
            wrapper.size.decrementAndGet();
            contextQueue.add(wrapper);
        } finally {
            poolLock.unlock();
        }

    }


    private void cleanupLoop() {
        while (true) {
            try {
                Thread.sleep(5 * 60 * 100); // 每5分钟检查一次
                if (unUsedPages.size() > 1) {
                    cleanupPages();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void cleanupPages() {
        poolLock.lock();
        try {
            Iterator<ContextWrapper> iterator = contextQueue.iterator();
            while (iterator.hasNext()) {
                ContextWrapper wrapper = iterator.next();
                if (wrapper.size.get() == 0) {
                    unUsedPages.removeIf(page -> {
                        ContextWrapper cw = pageContextMap.get(page);
                        return cw == wrapper;
                    });
                    wrapper.ctx.close();
                    iterator.remove();
                }
            }
        } finally {
            poolLock.unlock();
        }
    }

}
