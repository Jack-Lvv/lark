package com.cqupt.lark;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestGPTDemo {

    @Test
    public void test() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        String answer = model.chat("你是什么模型");
        System.out.println(answer);
    }

    @Test
    public void testDeepSeek() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-69e6710628df43daab681c347712d328")
                .modelName("deepseek-v3")
                .build();

        String answer = model.chat("你是什么模型");
        System.out.println(answer);
    }

    @Autowired
    private OpenAiChatModel model;
    @Test
    public void testSpringBootStarter() {

        // 自动封装http请求发送至大模型，自动build大模型配置，简化开发流程
        String answer = model.chat("我是谁");
        System.out.println(answer);

    }
}
