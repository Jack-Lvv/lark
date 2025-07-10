package com.cqupt.lark.api.service.impl;

import com.cqupt.lark.api.service.AbstractAutoTestService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import com.cqupt.lark.vector.service.SearchVectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class VisionAutoTestService extends AbstractAutoTestService {

    private final TestCasesTrans testCasesTrans;
    private final TestExecutorService testExecutorService;

    public VisionAutoTestService(
            @Value("${app.config.vision-max-retry-times}") Integer maxFailureTimes,
            ValidateService validateService, TestCasesTrans testCasesTrans,
            TestExecutorService testExecutorService, SearchVectorService searchVectorService) {
        super(validateService, maxFailureTimes, searchVectorService);
        this.testCasesTrans = testCasesTrans;
        this.testExecutorService = testExecutorService;
    }

    @Override
    public boolean executeTest(String standardStr, BrowserPageSupport browserPageSupport) throws InterruptedException {
        TestCaseVision testCaseVision;
        try {
            String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
            testCaseVision = testCasesTrans.transToJsonWithVision(standardCases);
        } catch (Throwable t) {
            throw new BusinessException(ExceptionEnum.JSON_TRANS_ERROR);
        }

        // 矫正大模型和页面的像素偏移量
        TestCaseVision testCaseVisionCorrected = OffsetCorrectUtils.correct(testCaseVision);

        return testExecutorService.executeWithVision(testCaseVisionCorrected, browserPageSupport);
    }

    @Override
    public String testTransByAi(String aCase, BrowserPageSupport browserPageSupport) throws IOException, InterruptedException {
        return testCasesTrans.transByVision(aCase, browserPageSupport);
    }
}
