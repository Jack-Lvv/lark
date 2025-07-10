package com.cqupt.lark.execute.service;

import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.translation.model.entity.TestCase;
import com.cqupt.lark.translation.model.entity.TestCaseVision;
import com.cqupt.lark.vector.model.dto.AnswerDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface TestExecutorService {

    Boolean execute(TestCase testCase, BrowserPageSupport browserPageSupport) throws InterruptedException;

    boolean executeWithVision(TestCaseVision testCaseVision, BrowserPageSupport browserPageSupport) throws InterruptedException;

    boolean executeWithAnswer(AnswerDTO answer, BrowserPageSupport browserPageSupport, String aCase,
                              SseEmitter emitter) throws InterruptedException, IOException;
}
