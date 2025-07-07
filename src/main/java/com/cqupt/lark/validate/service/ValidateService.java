package com.cqupt.lark.validate.service;

import com.cqupt.lark.execute.model.entity.TestResult;

import java.io.IOException;

public interface ValidateService {

    TestResult validate(byte[] oldScreenshot,byte[] newScreenshot, String aCase) throws IOException;
}
