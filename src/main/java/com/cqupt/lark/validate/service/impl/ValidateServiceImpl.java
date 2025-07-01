package com.cqupt.lark.validate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cqupt.lark.agent.service.Assistant;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.validate.service.ValidateService;
import dev.langchain4j.internal.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ValidateServiceImpl implements ValidateService {

    private final Assistant assistant;
    @Override
    public TestResult validate(byte[] buffer, String aCase) throws IOException {

        String result = assistant.chatWithValidate(aCase, buffer);

        return JSON.parseObject(result, new TypeReference<>(){});
    }
}
