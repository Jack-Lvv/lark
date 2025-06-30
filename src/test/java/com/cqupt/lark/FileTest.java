package com.cqupt.lark;

import com.cqupt.lark.agent.service.Assistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
public class FileTest {

    @Autowired
    private Assistant assistant;

    @Test
    public void testChatWithPicture() throws Exception {
        // 准备测试图片文件
        ClassPathResource imageResource = new ClassPathResource("picture/2025-06-27 151256.png");
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", // 参数名，需要与控制器方法中的@RequestParam名称匹配
                "test.png", // 文件名
                "image/png", // 内容类型
                imageResource.getInputStream() // 文件内容
        );

        String response = assistant.chatWithPicture("这张浏览器页面截图中，登录按钮的坐标是什么", imageFile);

        System.out.println(response);
    }

}
