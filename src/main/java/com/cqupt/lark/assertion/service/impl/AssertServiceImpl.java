package com.cqupt.lark.assertion.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cqupt.lark.agent.service.VisionAssistant;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.util.SubStringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AssertServiceImpl implements AssertService {

    private final VisionAssistant visionAssistant;

    @Override
    public AssertResult assertByVision(byte[] screenshot, String expectedResult) throws IOException {

        String result = visionAssistant.chatWithAssert(expectedResult, screenshot);

        String resultSubStr = SubStringUtils.subCasesUselessPart(result);

        return JSON.parseObject(resultSubStr, new TypeReference<>(){});

    }
}
