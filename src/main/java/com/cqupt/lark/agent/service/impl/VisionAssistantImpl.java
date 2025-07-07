package com.cqupt.lark.agent.service.impl;

import com.cqupt.lark.agent.service.VisionAssistant;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class VisionAssistantImpl implements VisionAssistant {

    private final OpenAiChatModel model;

    public VisionAssistantImpl(
            @Value("${open-ai.doubao-vision.base-url}") String baseUrl,
            @Value("${open-ai.doubao-vision.api-key}") String apiKey,
            @Value("${open-ai.doubao-vision.model-name}") String modelName) {
        model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

//    private final OllamaChatModel model;
//    public VisionAssistantImpl() {
//        model = OllamaChatModel.builder()
//                .baseUrl("http://localhost:11434")
//                .modelName("qwen2.5vl:7b-q8_0").build();
//            }


    @Override
    public String chatByVision(String InputMessage, byte[] imageData) throws IOException {

        String prompt;

        try (InputStream inputStream = VisionAssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/trans-by-vision.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        String base64Data = Base64.getEncoder().encodeToString(imageData);
        ImageContent imageContent = ImageContent.from(base64Data, "image/jpeg");
        return model.chat(SystemMessage.from(prompt), UserMessage.from(InputMessage), UserMessage.from(imageContent)).aiMessage().text();
    }

    @Override
    public String chatWithValidate(String inputMessage, byte[] oldImageData, byte[] newImageData) throws IOException {

        String prompt;

        try (InputStream inputStream = VisionAssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/validate.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        String oldBase64Data = Base64.getEncoder().encodeToString(oldImageData);
        ImageContent oldImageContent = ImageContent.from(oldBase64Data, "image/jpeg");

        String newBase64Data = Base64.getEncoder().encodeToString(newImageData);
        ImageContent newImageContent = ImageContent.from(newBase64Data, "image/jpeg");
        return model.chat(SystemMessage.from(prompt), UserMessage.from(inputMessage), UserMessage.from(oldImageContent), UserMessage.from(newImageContent)).aiMessage().text();
    }

    @Override
    public String chatWithAssert(String inputMessage, byte[] imageData) throws IOException {

        String prompt;
        try (InputStream inputStream = VisionAssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/assertion.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        String base64Data = Base64.getEncoder().encodeToString(imageData);
        ImageContent imageContent = ImageContent.from(base64Data, "image/jpeg");
        return model.chat(SystemMessage.from(prompt), UserMessage.from(inputMessage), UserMessage.from(imageContent)).aiMessage().text();
    }
}
