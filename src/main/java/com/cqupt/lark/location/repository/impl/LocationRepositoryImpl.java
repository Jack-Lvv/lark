package com.cqupt.lark.location.repository.impl;

import com.cqupt.lark.location.repository.LocationRepository;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Service;

@Service
public class LocationRepositoryImpl implements LocationRepository {

    @Override
    public Locator getByText(String text, Page page) {
        return page.getByText(text);
    }


    @Override
    public Locator getByLocator(String text, Page page) {
        return page.locator(text);
    }

    @Override
    public Locator getButtonByText(String text, Page page) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(text).setExact(false));
    }

    @Override
    public Locator getTextBoxByText(String text, Page page) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(text).setExact(false));
    }

    @Override
    public Locator getDialogByText(String text, Page page) {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(text).setExact(false));
    }

    @Override
    public Locator getNavigationByText(String text, Page page) {
        return page.getByRole(AriaRole.NAVIGATION,
                new Page.GetByRoleOptions().setName(text).setExact(false));
    }

    @Override
    public Locator getSwitchByText(String text, Page page) {
        return page.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName(text).setExact(false));
    }

    @Override
    public Locator getLabelByText(String text, Page page) {
        return page.getByLabel(text);
    }

    @Override
    public Locator getPlaceholderByText(String text, Page page) {
        return page.getByPlaceholder(text);
    }

    @Override
    public Locator getTestIdByText(String text, Page page) {
        return page.getByTestId(text);
    }

}
