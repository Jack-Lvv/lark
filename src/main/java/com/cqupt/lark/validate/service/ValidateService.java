package com.cqupt.lark.validate.service;

import com.cqupt.lark.execute.model.entity.TestResult;

public interface ValidateService {

    TestResult validate(byte[] buffer, String aCase);
}
