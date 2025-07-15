package com.cqupt.lark.util;

import com.cqupt.lark.agent.service.Assistant;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class RecommendUtils {

    private final Assistant assistant;

    @Setter
    private static String lastTestCase = "首次执行测试用例，无法提供上次测试用例";

    public String getRecommendTestCases() throws IOException {
        return assistant.chatWithRecommend(lastTestCase);
    }

}
