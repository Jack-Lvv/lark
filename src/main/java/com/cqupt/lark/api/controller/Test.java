package com.cqupt.lark.api.controller;

import com.cqupt.lark.api.model.dto.RequestDTO;
import com.cqupt.lark.browser.BrowserPageSupport;
import com.cqupt.lark.browser.BrowserSession;
import com.cqupt.lark.util.UrlStringAdder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class Test {

    private final BrowserSession browserSession;


    private final ExecutorService executor;

    @GetMapping(value = "/api/test/v1")
    public void test(RequestDTO request) throws InterruptedException {

        BrowserPageSupport browserPageSupport = browserSession.getBrowserPageSupport();

        executor.execute(() -> {
                    try {
                        browserPageSupport.navigate(UrlStringAdder.urlStrAdd(request.getUrl()));
                        browserPageSupport.screenshot();
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    try {
                        browserPageSupport.close();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

    }
}


