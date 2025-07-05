package com.cqupt.lark.exception.handler;

import com.cqupt.lark.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
        emitter.send(SseEmitter.event()
                .name("result") // 自定义事件类型
                .data(e.getMessage()));
        emitter.complete();
        return e.getMessage();
    }

}
