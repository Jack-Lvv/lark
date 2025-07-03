package com.cqupt.lark.translation.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cqupt.lark.agent.service.Assistant;
import com.cqupt.lark.agent.service.VisionAssistant;
import com.cqupt.lark.browser.service.StartBrowserService;
import com.cqupt.lark.translation.model.dto.TestCaseDTO;
import com.cqupt.lark.translation.model.dto.TestCaseVisionDTO;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.translation.model.enums.CaseType;
import com.cqupt.lark.translation.model.enums.LocatorType;
import com.cqupt.lark.translation.service.TestCasesTrans;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestCasesTransImpl implements TestCasesTrans {

    private final Assistant assistant;
    private final VisionAssistant visionAssistant;
    @Override
    public String trans(String description, Page page) throws IOException {

        String htmlContext = page.content();
        String subHtmlContext = subHtmlContext(htmlContext);
        String outMessageByAi = assistant.chatWithTranslation(description, subHtmlContext);
        log.info("大模型输出测试用例json格式: {}", outMessageByAi);
        return outMessageByAi;
    }

    @Override
    public TestCase transToJson(String outMessageByAi) {

        TestCaseDTO testCaseDTO = JSON.parseObject(outMessageByAi, new TypeReference<>(){});

        return convertToTestCase(testCaseDTO);
    }

    @Override
    public String transByVision(String aCase, StartBrowserService startBrowserService) throws IOException {
        String outMessageByAi = visionAssistant.chatByVision(aCase, startBrowserService.screenshot());
        log.info("视觉大模型输出测试用例的json格式: {}", outMessageByAi);
        return outMessageByAi;
    }

    @Override
    public TestCaseVision transToJsonWithVision(String standardCases) {
        TestCaseVisionDTO testCaseVisionDTO = JSON.parseObject(standardCases, new TypeReference<>() {});
        return convertToTestCaseVision(testCaseVisionDTO);
    }

    private TestCaseVision convertToTestCaseVision(TestCaseVisionDTO dto) {
        return TestCaseVision.builder()
                .caseType(CaseType.valueOf(dto.getCaseType()))
                .caseValue(dto.getCaseValue())
                .xUp(dto.getXUp())
                .yUp(dto.getYUp())
                .xDown(dto.getXDown())
                .yDown(dto.getYDown())
                .build();
    }

    public String subHtmlContext(String htmlContext) {
        return htmlContext.replaceAll("<image[^>]*xlink:href=\"data:image/[^;]+;base64,[^\"]+\"", "<image>")
                .replaceAll("<path[^>]*>", "")              // 移除矢量图路径
                .replaceAll("(?s)<script.*?</script>", "")  // 移除脚本
                .replaceAll("<style.*?</style>", "")        // 移除样式
                .replaceAll("\\s+", " ")                   // 压缩空白
                .replaceAll("(?s)<!--.*?-->", "")       // 移除注释
                .replaceAll("(?i)<meta[^>]*>", "");
    }

    private TestCase convertToTestCase(TestCaseDTO dto) {
        return TestCase.builder()
                .caseType(CaseType.valueOf(dto.getCaseType()))
                .locatorType(LocatorType.valueOf(dto.getLocatorType()))
                .caseValue(dto.getCaseValue())
                .locatorValue(dto.getLocatorValue())
                .build();
    }

}
