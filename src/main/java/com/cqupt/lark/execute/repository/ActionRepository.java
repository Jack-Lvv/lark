package com.cqupt.lark.execute.repository;


import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public interface ActionRepository {

    // 启动浏览器
    Page startBrowser();

    void clickButton(Locator locator);
}
