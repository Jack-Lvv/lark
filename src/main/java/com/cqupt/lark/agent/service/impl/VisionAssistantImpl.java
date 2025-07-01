package com.cqupt.lark.agent.service.impl;

import com.cqupt.lark.agent.service.VisionAssistant;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VisionAssistantImpl implements VisionAssistant {

    @Value("${open-ai.doubao-vision.base-url}")
    private String baseUrl;
    @Value("${open-ai.doubao-vision.api-key}")
    private String apiKey;
    @Value("${open-ai.doubao-vision.model-name}")
    private String modelName;


    @Override
    public String chatByVision(String InputMessage, byte[] imageData) throws IOException {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();
        String prompt;

        try (InputStream inputStream = AssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/trans-by-vision.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String base64Data = Base64.getEncoder().encodeToString(imageData);
        ImageContent imageContent = ImageContent.from(base64Data, "image/png");
        return model.chat(SystemMessage.from(prompt), UserMessage.from(InputMessage), UserMessage.from(imageContent)).aiMessage().text();
    }
}
