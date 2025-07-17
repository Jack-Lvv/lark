package com.cqupt.lark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LarkApplication.class, args);
    }

}
