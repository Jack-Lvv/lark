package com.cqupt.lark;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class TestGPTDemo {

    @Value("${open-ai.doubao.base-url}")
    private String baseUrl;
    @Value("${open-ai.doubao.api-key}")
    private String apiKey;
    @Value("${open-ai.doubao.model-name}")
    private String modelName;


    @Test
    public void test() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .metadata(Map.of("thinking", "{\"type\":\"disabled\"}"))
                        .build())
                .build();
        String answer = model.chat(UserMessage.from("你开启深度思考模式了吗")).aiMessage().text();
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
