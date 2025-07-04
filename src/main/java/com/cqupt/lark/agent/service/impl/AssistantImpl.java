package com.cqupt.lark.agent.service.impl;

import com.cqupt.lark.agent.service.Assistant;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AssistantImpl implements Assistant {

    @Value("${open-ai.doubao.base-url}")
    private String baseUrl;
    @Value("${open-ai.doubao.api-key}")
    private String apiKey;
    @Value("${open-ai.doubao.model-name}")
    private String modelName;

    private OpenAiChatModel model = OpenAiChatModel.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .modelName(modelName)
            .logRequests(true)
            .logResponses(true)
            .build();
    @Override
    public String chatWithTranslation(String InputMessage, String HtmlContext) throws IOException {
        String prompt;

        try (InputStream inputStream = AssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/translation.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        return model.chat(SystemMessage.from(prompt), UserMessage.from(InputMessage), UserMessage.from(HtmlContext)).aiMessage().text();
    }

    @Override
    public String chatWithValidate(String inputMessage, byte[] imageData) throws IOException {
        String prompt;

        try (InputStream inputStream = AssistantImpl.class
                .getClassLoader()
                .getResourceAsStream("prompt/validate.txt")) {
            if (inputStream == null) {
                throw new IOException("文件不存在！");
            }
            prompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        String base64Data = Base64.getEncoder().encodeToString(imageData);
        ImageContent imageContent = ImageContent.from(base64Data, "image/png");
        return model.chat(SystemMessage.from(prompt), UserMessage.from(inputMessage), UserMessage.from(imageContent)).aiMessage().text();
    }

    @Override
    public String chatWithPicture(String InputMessage, MultipartFile image) throws IOException {
        return chatWithPicture(InputMessage, image.getBytes());
    }

    @Override
    public String chatWithPicture(String InputMessage, byte[] imageData) {
        String base64Data = Base64.getEncoder().encodeToString(imageData);
        ImageContent imageContent = ImageContent.from(base64Data, "image/png");
        UserMessage imageMessage = UserMessage.from(imageContent);
        return model.chat(imageMessage, UserMessage.from(InputMessage)).aiMessage().text();
    }

    @Override
    public String chatWithVideo(String InputMessage, MultipartFile video) throws IOException {
        return chatWithVideo(InputMessage, video.getBytes());
    }

    @Override
    public String chatWithVideo(String InputMessage, byte[] videoData) {
        String base64Data = Base64.getEncoder().encodeToString(videoData);
        VideoContent videoContent = VideoContent.from(base64Data, "video/mp4");
        UserMessage videoMessage = UserMessage.from(videoContent);
        return model.chat(videoMessage, UserMessage.from(InputMessage)).aiMessage().text();
    }

    @Override
    public String chatWithVideoAndPicture(String InputMessage, MultipartFile image, MultipartFile video) throws IOException {
        return chatWithVideoAndPicture(InputMessage, image.getBytes(), video.getBytes());
    }

    @Override
    public String chatWithVideoAndPicture(String InputMessage, byte[] imageData, byte[] videoData) {
        String base64ImageData = Base64.getEncoder().encodeToString(imageData);
        String base64VideoData = Base64.getEncoder().encodeToString(videoData);
        ImageContent imageContent = ImageContent.from(base64ImageData, "image/jpg");
        UserMessage imageMessage = UserMessage.from(imageContent);
        VideoContent videoContent = VideoContent.from(base64VideoData, "video/mp4");
        UserMessage videoMessage = UserMessage.from(videoContent);
        return model.chat(imageMessage, videoMessage, UserMessage.from(InputMessage)).aiMessage().text();
    }


}
