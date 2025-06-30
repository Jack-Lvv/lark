package com.cqupt.lark.execute.repository.impl;

import com.cqupt.lark.execute.repository.ActionRepository;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

@Service
public class ActionRepositoryImpl implements ActionRepository {

    @Override
    public Page startBrowser() {
        try (Playwright playwright = Playwright.create()) {
            // 2. 启动浏览器(Chromium)
            // 设为false可看到浏览器, 减慢操作速度
            return playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(false))
                    .newContext()
                    .newPage();
        }

    }

    @Override
    public void clickButton(Locator locator) {
        locator.waitFor();
        locator.click();
    }

}
