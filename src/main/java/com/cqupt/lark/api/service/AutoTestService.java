package com.cqupt.lark.api.service;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.browser.BrowserPageSupport;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface AutoTestService {

    boolean autoTest(BrowserPageSupport browserPageSupport, SseEmitter emitter, String aCase) throws IOException;
}
