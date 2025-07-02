package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.api.model.vo.ResponseVO;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.Executor;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.cqupt.lark.util.OffsetCorrectUtils;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UITestController {

    private final TestCasesTrans testCasesTrans;
    private final Executor executor;
    private final ValidateService validateService;

    @Value("${app.config.max-retry-times}")
    private int maxFailureTimes;

    @PostMapping("/api/test")
    public ResponseVO test(@RequestBody RequestDTO request) {
        log.info("测试网址: {}", request.getUrl());
        log.info("测试用例描述: {}", request.getDescription());

        // 获取项目根目录路径
        Path resourcesPath = Paths.get("src/main/resources/mock/auth.json").toAbsolutePath();

        try {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                         .setHeadless(false).setChannel("chrome").setSlowMo(1000));
                 BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                         .setRecordVideoDir(Paths.get("src/main/resources/static/videos"))
                         .setRecordVideoSize(1280, 720)
                         .setStorageStatePath(resourcesPath)); // 设置视频尺寸
                 Page page = context.newPage()) {

                page.navigate(request.getUrl());

                String[] cases = SubStringUtils.subCasesStr(request.getDescription());
                int failureTimes = 0, index = 0;
                List<TestResult> testResults = new ArrayList<>();

                while (index < cases.length && failureTimes <= maxFailureTimes) {
                    log.info("开始测试第{}个用例: {}", index + 1, cases[index]);

                    //String standardCases = testCasesTrans.trans(cases[index], page);
                    String standardStr = testCasesTrans.transByVision(cases[index], page);

                    String standardCases = SubStringUtils.subCasesUselessPart(standardStr);

                    //TestCase testCase = new TestCase();
                    TestCaseVision testCaseVision = new TestCaseVision();
                    try {
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
                    if (executor.executeWithVision(testCaseVisionCorrected, page)) {
                        testResult = validateService.validate(page.screenshot(), cases[index]);
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
                if (failureTimes > maxFailureTimes) {
                    testResults.add(TestResult.builder()
                                    .status(false)
                                    .description("达到最大重试次数仍未执行成功")
                                    .build());
                }

                String videoPath = SubStringUtils.subVideosPath(page.video().path().toString());
                return ResponseVO.builder()
                        .success(true)
                        .message(testResults.toString())
                        // .video("videos/ZongBianShi-HuYongQiu_9b7a98caab886e25f0efe8992df6ae80.mp4")
                        .video(videoPath)
                        .build();
            }

        } catch (Exception e) {
            return ResponseVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }

    }

}
