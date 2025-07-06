package com.cqupt.lark.exception.handler;

import com.cqupt.lark.exception.BusinessException;
import com.cqupt.lark.util.EmitterSendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class SSEExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handlerBusinessException(BusinessException e, SseEmitter emitter) throws IOException {
        log.error(e.getMessage());
        EmitterSendUtils.send(emitter, "result", false, e.getMessage());
        return e.getMessage();
    }

}
