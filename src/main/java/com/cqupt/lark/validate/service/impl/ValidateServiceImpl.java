package com.cqupt.lark.validate.service.impl;

import com.cqupt.lark.execute.model.entity.TestResult;
import com.cqupt.lark.validate.service.ValidateService;
import org.springframework.stereotype.Service;

@Service
public class ValidateServiceImpl implements ValidateService {

    @Override
    public TestResult validate(byte[] buffer, String aCase) {

        return TestResult.builder()
                .status(true)
                .description("验证通过")
                .build();
    }
}
