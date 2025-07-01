package com.cqupt.lark.validate.service;

import com.cqupt.lark.execute.model.entity.TestResult;

import java.io.IOException;

public interface ValidateService {

    TestResult validate(byte[] buffer, String aCase) throws IOException;
}
