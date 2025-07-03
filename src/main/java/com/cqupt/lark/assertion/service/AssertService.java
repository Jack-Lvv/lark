package com.cqupt.lark.assertion.service;

import com.cqupt.lark.assertion.model.entity.AssertResult;

import java.io.IOException;

public interface AssertService {

    AssertResult assertByVision(byte[] screenshot, String expectedResult) throws IOException;
}
