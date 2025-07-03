package com.cqupt.lark.agent.service;

import dev.langchain4j.service.spring.AiService;

import java.io.IOException;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

public interface VisionAssistant {

    String chatByVision(String InputMessage, byte[] imageData) throws IOException;

    String chatWithValidate(String inputMessage, byte[] imageData) throws IOException;

    String chatWithAssert(String expectedResult, byte[] screenshot) throws IOException;
}
