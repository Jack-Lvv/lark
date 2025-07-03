package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.api.model.vo.ResponseVO;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.browser.service.StartBrowserService;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.TestExecutorService;
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

        StartBrowserService startBrowserService = StartBrowserService.getInstance();
        try {

            startBrowserService.navigate(request.getUrl());

            String[] cases = SubStringUtils.subCasesStr(request.getDescription());
            int failureTimes = 0, index = 0;
            List<TestResult> testResults = new ArrayList<>();

            while (index < cases.length && failureTimes <= maxFailureTimes) {
                log.info("开始测试第{}个用例: {}", index + 1, cases[index]);

                //String standardCases = testCasesTrans.trans(cases[index], page);
                String standardStr = testCasesTrans.transByVision(cases[index], startBrowserService);

                //TestCase testCase = new TestCase();
                TestCaseVision testCaseVision = new TestCaseVision();
                try {
                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);
                    //testCase = testCasesTrans.transToJson(standardCases);
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
                if (testExecutorService.executeWithVision(testCaseVisionCorrected, startBrowserService)) {
                    testResult = validateService.validate(startBrowserService.screenshot(), cases[index]);
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
            }

            AssertResult assertResult = null;
            if (failureTimes > maxFailureTimes) {
                testResults.add(TestResult.builder()
                        .status(false)
                        .description("达到最大重试次数仍未执行成功")
                        .build());
            } else if (request.getExpectedResult() != null && !request.getExpectedResult().isEmpty()) {
                assertResult = assertService.assertByVision(startBrowserService.screenshot(), request.getExpectedResult());
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
            startBrowserService.close();
        }

    }

}
