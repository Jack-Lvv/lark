package com.cqupt.lark.api.service.impl;

import com.cqupt.lark.api.service.AbstractAutoTestService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import com.cqupt.lark.vector.service.SearchVectorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CodeAutoTestService extends AbstractAutoTestService {

    private final TestCasesTrans testCasesTrans;
    private final TestExecutorService testExecutorService;

    public CodeAutoTestService(@Value("${app.config.code-max-retry-times}") Integer maxFailureTimes,
                               ValidateService validateService, TestCasesTrans testCasesTrans,
                               TestExecutorService testExecutorService, SearchVectorService searchVectorService) {
        super(validateService, maxFailureTimes, searchVectorService);
        this.testCasesTrans = testCasesTrans;
        this.testExecutorService = testExecutorService;
    }

    @Override
    public boolean executeTest(String standardStr, BrowserPageSupport browserPageSupport) throws InterruptedException {
        TestCase testCase;
        try {
            String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
            testCase = testCasesTrans.transToJson(standardCases);
        } catch (Throwable t) {
            throw new BusinessException(ExceptionEnum.JSON_TRANS_ERROR);
        }

        return testExecutorService.execute(testCase, browserPageSupport);
    }

    @Override
    public String testTransByAi(String aCase, BrowserPageSupport browserPageSupport) throws IOException, InterruptedException {
        return testCasesTrans.trans(aCase, browserPageSupport);

    }
}
