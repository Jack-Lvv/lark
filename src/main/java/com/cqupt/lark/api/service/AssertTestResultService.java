package com.cqupt.lark.api.service;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.assertion.model.entity.AssertResult;
import com.cqupt.lark.assertion.service.AssertService;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.exception.enums.ExceptionEnum;
import com.cqupt.lark.util.EmitterSendUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AssertTestResultService {

    private final AssertService assertService;
    public void assertResult(SseEmitter emitter, RequestDTO request, BrowserPageSupport browserPageSupport) {
        AssertResult assertResult = null;
        if (request.getExpectedResult() != null && !request.getExpectedResult().isEmpty()) {
            try {
                assertResult = assertService.assertByVision(browserPageSupport.screenshot(), request.getExpectedResult());
            } catch (IOException | InterruptedException e) {
                throw new BusinessException(ExceptionEnum.ASSERT_ERROR);
            }
        }

        if (assertResult != null && assertResult.getStatus()) {
            try {
                EmitterSendUtils.send(emitter, "result",
                        true, "断言成功，" + assertResult.getDescription());
            } catch (IOException e) {
                throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
            }
        } else if (assertResult != null) {
            try {
                EmitterSendUtils.send(emitter, "result",
                        false, "断言失败，" + assertResult.getDescription());
            } catch (IOException e) {
                throw new BusinessException(ExceptionEnum.SSE_SEND_ERROR);
            }
        }
    }
}
