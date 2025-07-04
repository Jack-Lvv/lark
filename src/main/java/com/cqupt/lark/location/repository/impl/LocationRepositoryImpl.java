package com.cqupt.lark.location.repository.impl;

import com.cqupt.lark.browser.service.BrowserPageSupport;
import com.cqupt.lark.location.repository.LocationRepository;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Service;

@Service
public class LocationRepositoryImpl implements LocationRepository {

    @Override
    public Locator getByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByText(text);
    }


    @Override
    public Locator getByLocator(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByLocator(text);
    }

    @Override
    public Locator getButtonByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByRole(AriaRole.BUTTON, text);
    }

    @Override
    public Locator getTextBoxByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByRole(AriaRole.TEXTBOX, text);
    }

    @Override
    public Locator getDialogByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByRole(AriaRole.DIALOG, text);
    }

    @Override
    public Locator getNavigationByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByRole(AriaRole.NAVIGATION, text);

    }

    @Override
    public Locator getSwitchByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getByRole(AriaRole.SWITCH, text);

    }

    @Override
    public Locator getLabelByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getLabelByText(text);
    }

    @Override
    public Locator getPlaceholderByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getPlaceholderByText(text);
    }

    @Override
    public Locator getTestIdByText(String text, BrowserPageSupport browserPageSupport) {
        return browserPageSupport.getTestIdByText(text);
    }

}
