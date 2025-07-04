package com.cqupt.lark.location.service;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.location.repository.LocationRepository;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocatorServiceImpl implements LocatorService {

    private final LocationRepository locationRepository;

    @Override
    public Locator getLocatorByJson(TestCase testCase, BrowserPageSupport browserPageSupport) {
        return switch (testCase.getLocatorType()) {
            case Button -> locationRepository.getButtonByText(testCase.getLocatorValue(), browserPageSupport);
            case TextBox -> locationRepository.getTextBoxByText(testCase.getLocatorValue(), browserPageSupport);
            case Dialog -> locationRepository.getDialogByText(testCase.getLocatorValue(), browserPageSupport);
            case Navigation -> locationRepository.getNavigationByText(testCase.getLocatorValue(), browserPageSupport);
            case Switch -> locationRepository.getSwitchByText(testCase.getLocatorValue(), browserPageSupport);
            case Label -> locationRepository.getLabelByText(testCase.getLocatorValue(), browserPageSupport);
            case Locator -> locationRepository.getByLocator(testCase.getLocatorValue(), browserPageSupport);
            case Placeholder -> locationRepository.getPlaceholderByText(testCase.getLocatorValue(), browserPageSupport);
            case Text -> locationRepository.getByText(testCase.getLocatorValue(), browserPageSupport);
            case TestId -> locationRepository.getTestIdByText(testCase.getLocatorValue(), browserPageSupport);
        };
    }
}
