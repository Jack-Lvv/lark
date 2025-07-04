package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.api.model.vo.ResponseVO;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.browser.service.BrowserPageSupport;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.TestExecutorService;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UITestController {

    private final TestCasesTrans testCasesTrans;
    private final TestExecutorService testExecutorService;
    private final ValidateService validateService;
    private final AssertService assertService;

    @Value("${app.config.max-retry-times}")
    private int maxFailureTimes;

    @PostMapping("/api/test")
    public ResponseVO test(@RequestBody RequestDTO request) {
        log.info("测试网址: {}", request.getUrl());
        log.info("测试用例描述: {}", request.getDescription());
        log.info("预期结果描述: {}", request.getExpectedResult());

        BrowserPageSupport browserPageSupport = BrowserPageSupport.getInstance();
        try {

            browserPageSupport.navigate(request.getUrl());
            browserPageSupport.setStarted();

            String[] cases = SubStringUtils.subCasesStr(request.getDescription());
            int failureTimes = 0, index = 0;
            List<TestResult> testResults = new ArrayList<>();

            while (index < cases.length && failureTimes <= maxFailureTimes) {
                log.info("开始测试第{}个用例: {}", index + 1, cases[index]);


                String standardStr = testCasesTrans.transByVision(cases[index], browserPageSupport);

                TestCaseVision testCaseVision;
                try {
                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
                    testCaseVision = testCasesTrans.transToJsonWithVision(standardCases);
                } catch (Exception e) {
                    failureTimes++;
                    log.error("第{}个用例json转换失败: {}", index + 1, e.getMessage());
                    continue;
                }

                // 矫正大模型和页面的像素偏移量
                TestCaseVision testCaseVisionCorrected = OffsetCorrectUtils.correct(testCaseVision);

                TestResult testResult = new TestResult();
                //if (executor.execute(testCase, page)) {
                if (testExecutorService.executeWithVision(testCaseVisionCorrected, browserPageSupport)) {
                    testResult = validateService.validate(browserPageSupport.screenshot(), cases[index]);
                } else {
                    testResult.setStatus(false);
                    testResult.setDescription("定位组件失败");
                }

                testResults.add(testResult);
                if (testResult.getStatus()) {
                    index++;
                    failureTimes = 0;
                    log.info("测试#{}成功...", index + 1);
                } else {
                    failureTimes++;
                    log.info("测试#{}失败，进行重试...", index + 1);
                }

                // 重试次数满后，进行兜底定位操作
                if (failureTimes > maxFailureTimes) {
                    log.info("开始进行兜底定位...");
                    try {
                        String standardCases = testCasesTrans.trans(cases[index], browserPageSupport);
                        String casesAfterCorrect = SubStringUtils.subCasesUselessPart(standardCases);
                        TestCase testCase = testCasesTrans.transToJson(casesAfterCorrect);
                        testExecutorService.execute(testCase, browserPageSupport);
                        TestResult testResultByOCR = validateService.validate(browserPageSupport.screenshot(), cases[index]);
                        if (!testResult.getStatus()) {
                            throw new Exception("执行操作失败，"+ testResultByOCR.getDescription());
                        } else {
                            index++;
                            failureTimes = 0;
                            log.info("测试#{}成功...", index + 1);
                        }
                    } catch (Throwable t) {
                        log.info("兜底定位失败: {}", t.getMessage());
                    }
                }
            }

            AssertResult assertResult = null;
            if (failureTimes > maxFailureTimes) {
                testResults.add(TestResult.builder()
                        .status(false)
                        .description("达到最大重试次数仍未执行成功")
                        .build());
            } else if (request.getExpectedResult() != null && !request.getExpectedResult().isEmpty()) {
                assertResult = assertService.assertByVision(browserPageSupport.screenshot(), request.getExpectedResult());
            }

            if (assertResult != null) {
                return ResponseVO.builder()
                        .success(true)
                        .message(testResults.toString())
                        // .video("videos/ZongBianShi-HuYongQiu_9b7a98caab886e25f0efe8992df6ae80.mp4")
                        .assertMessage(assertResult.toString())
                        .build();
            } else {
                return ResponseVO.builder()
                        .success(true)
                        .message(testResults.toString())
                        // .video("videos/ZongBianShi-HuYongQiu_9b7a98caab886e25f0efe8992df6ae80.mp4")
                        .assertMessage("预期结果为空，未执行断言")
                        .build();
            }

        } catch (Exception e) {
            return ResponseVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } finally {
            browserPageSupport.close();
        }

    }

}
