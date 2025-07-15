package com.cqupt.lark.api.controller;

import com.cqupt.lark.util.RecommendUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendUtils recommendUtils;
    @GetMapping("/api/recommend")
    public String recommend() throws IOException {
        return recommendUtils.getRecommendTestCases();
    }
}
