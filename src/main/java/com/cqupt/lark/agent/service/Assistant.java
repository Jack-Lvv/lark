package com.cqupt.lark.agent.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface Assistant {

    /**
     * 大模型聊天, 使用prompt
     * @param InputMessage 用户输入的聊天消息
     * @return OutputMessage 模型生成的回复消息
     */
    String chatWithTranslation(String InputMessage, String HtmlContext) throws IOException;

    String chatWithValidate(String inputMessage, byte[] imageData) throws IOException;

    String chatWithPicture(String InputMessage, MultipartFile image) throws IOException;

    String chatWithPicture(String InputMessage, byte[] imageData) throws IOException;

    String chatWithVideo(String InputMessage, MultipartFile video) throws IOException;

    String chatWithVideo(String InputMessage, byte[] videoData) throws IOException;

    String chatWithVideoAndPicture(String InputMessage, MultipartFile image, MultipartFile video) throws IOException;

    String chatWithVideoAndPicture(String InputMessage, byte[] imageData, byte[] videoData) throws IOException;
}
