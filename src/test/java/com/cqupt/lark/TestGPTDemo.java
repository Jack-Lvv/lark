package com.cqupt.lark;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestGPTDemo {

    @Value("${open-ai.doubao-vision.base-url}")
    private String baseUrl;
    @Value("${open-ai.doubao-vision.api-key}")
    private String apiKey;
    @Value("${open-ai.doubao-vision.model-name}")
    private String modelName;


    @Test
    public void test() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();
        String answer = model.chat(UserMessage.from("你是什么模型")).aiMessage().text();
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


}
