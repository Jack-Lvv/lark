package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.api.model.vo.ResponseVO;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.execute.service.Executor;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.service.TestCasesTrans;
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
        log.info("request: {}", request.getUrl());
        log.info("description: {}", request.getDescription());

        try {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                         .setHeadless(false).setChannel("chrome").setSlowMo(1000));
                 BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                         .setRecordVideoDir(Paths.get("src/main/resources/static/videos"))
                         .setRecordVideoSize(1280, 720)); // 设置视频尺寸);
                 Page page = context.newPage()) {

                page.navigate(request.getUrl());

                String[] cases = SubStringUtils.subCasesStr(request.getDescription());
                int failureTimes = 0, index = 0;
                List<TestResult> testResults = new ArrayList<>();

                while (index < cases.length && failureTimes <= maxFailureTimes) {

                    String standardCases = testCasesTrans.trans(cases[index], page);
                    TestCase testCase = testCasesTrans.transToJson(standardCases);

                    TestResult testResult = new TestResult();
                    if (executor.execute(testCase, page)) {
                        testResult = validateService.validate(page.screenshot(), cases[index]);
                    } else {
                        testResult.setStatus(false);
                        testResult.setDescription("定位组件失败");
                    }

                    testResults.add(testResult);
                    if (testResult.getStatus()) {
                        index++;
                        failureTimes = 0;
                        log.info("测试#{}成功...", index);
                    } else {
                        failureTimes++;
                        log.info("测试#{}失败，进行重试...", index);
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
