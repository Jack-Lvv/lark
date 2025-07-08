package com.cqupt.lark.browser;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Getter
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BrowserSession {

    private final BrowserPageSupport browserPageSupport;

    @Autowired
    public BrowserSession(PlaywrightPoolManager playwrightPoolManager) {
        this.browserPageSupport = new BrowserPageSupport(playwrightPoolManager);
    }

    @PreDestroy
    public void close() {
        browserPageSupport.close();
    }

}
