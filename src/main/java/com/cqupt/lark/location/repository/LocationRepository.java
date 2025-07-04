package com.cqupt.lark.location.repository;

import com.cqupt.lark.browser.service.BrowserPageSupport;
import com.microsoft.playwright.Locator;

public interface LocationRepository {

    // 通过文本内容定位
    Locator getByText(String text, BrowserPageSupport browserPageSupport);

    // 通过定位器定位
    Locator getByLocator(String text, BrowserPageSupport browserPageSupport);

    // 通过文本定位按钮
    Locator getButtonByText(String text, BrowserPageSupport browserPageSupport);

    // 通过文本定位文本框
    Locator getTextBoxByText(String text, BrowserPageSupport browserPageSupport);

    // 通过文本定位对话框
    Locator getDialogByText(String text, BrowserPageSupport browserPageSupport);

    // 通过文本定位导航栏
    Locator getNavigationByText(String text, BrowserPageSupport browserPageSupport);

    Locator getSwitchByText(String text, BrowserPageSupport browserPageSupport);

    Locator getLabelByText(String text, BrowserPageSupport browserPageSupport);

    Locator getPlaceholderByText(String text, BrowserPageSupport browserPageSupport);

    Locator getTestIdByText(String text, BrowserPageSupport browserPageSupport);
}
