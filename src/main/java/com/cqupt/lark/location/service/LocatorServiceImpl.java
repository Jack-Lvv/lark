package com.cqupt.lark.location.service;

import com.cqupt.lark.location.repository.LocationRepository;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocatorServiceImpl implements LocatorService {

    private final LocationRepository locationRepository;

    @Override
    public Locator getLocatorByJson(TestCase testCase, Page page) {
        return switch (testCase.getLocatorType()) {
            case Button -> locationRepository.getButtonByText(testCase.getLocatorValue(), page);
            case TextBox -> locationRepository.getTextBoxByText(testCase.getLocatorValue(), page);
            case Dialog -> locationRepository.getDialogByText(testCase.getLocatorValue(), page);
            case Navigation -> locationRepository.getNavigationByText(testCase.getLocatorValue(), page);
            case Switch -> locationRepository.getSwitchByText(testCase.getLocatorValue(), page);
            case Label -> locationRepository.getLabelByText(testCase.getLocatorValue(), page);
            case Locator -> locationRepository.getByLocator(testCase.getLocatorValue(), page);
            case Placeholder -> locationRepository.getPlaceholderByText(testCase.getLocatorValue(), page);
            case Text -> locationRepository.getByText(testCase.getLocatorValue(), page);
            case TestId -> locationRepository.getTestIdByText(testCase.getLocatorValue(), page);
        };
    }
}
