package com.cqupt.lark.location.repository;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public interface LocationRepository {

    // 通过文本内容定位
    Locator getByText(String text, Page page);

    // 通过定位器定位
    Locator getByLocator(String text, Page page);

    // 通过文本定位按钮
    Locator getButtonByText(String text, Page page);

    // 通过文本定位文本框
    Locator getTextBoxByText(String text, Page page);

    // 通过文本定位对话框
    Locator getDialogByText(String text, Page page);

    // 通过文本定位导航栏
    Locator getNavigationByText(String text, Page page);

    Locator getSwitchByText(String text, Page page);
}
