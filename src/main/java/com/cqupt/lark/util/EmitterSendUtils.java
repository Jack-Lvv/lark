package com.cqupt.lark.util;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

public class EmitterSendUtils {

    public static void send(SseEmitter emitter, String name, boolean state, String text) throws IOException {
        emitter.send(SseEmitter.event()
                .name(name)
                .data(Map.of("state", state,
                        "text", text)));
    }
}
