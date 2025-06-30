package com.cqupt.lark.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SubStringUtilsTest {

    @Test
    void subCasesStr() {
        System.out.println("1.登录\n" +
                "2.点击登录按钮\n" +
                "3.输入手机号");
        System.out.println(Arrays.toString(SubStringUtils.subCasesStr("1.登录\n" +
                "2.点击登录按钮\n" +
                "3.输入手机号")));
    }
}