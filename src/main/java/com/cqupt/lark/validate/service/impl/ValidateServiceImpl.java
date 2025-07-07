package com.cqupt.lark.validate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cqupt.lark.agent.service.VisionAssistant;
import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.util.SubStringUtils;
import com.cqupt.lark.validate.service.ValidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ValidateServiceImpl implements ValidateService {

    private final VisionAssistant visionAssistant;
    @Override
    public TestResult validate(byte[] oldScreenshot, byte[] newScreenshot, String aCase) throws IOException {

        String result = visionAssistant.chatWithValidate(aCase,oldScreenshot, newScreenshot);

        String resultSubStr = SubStringUtils.subCasesUselessPart(result);

        return JSON.parseObject(resultSubStr, new TypeReference<>(){});
    }
}
