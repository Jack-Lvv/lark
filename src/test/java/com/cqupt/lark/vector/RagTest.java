package com.cqupt.lark.vector;

import com.cqupt.lark.vector.model.entity.QaRecord;
import com.cqupt.lark.vector.service.VectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RagTest {

    @Autowired
    private VectorService vectorService;
    @Test
    public void add() {
        vectorService.initMilvusCollection();
        vectorService.saveQaRecord(QaRecord.builder()
                        .url("baidu.com")
                        .question("如何使用Milvus")
                        .answer("使用Milvus")
                        .isCorrect(true)
                        .build());
    }

    @Test
    public void query() {
        System.out.println(vectorService.searchSimilarTexts(QaRecord.builder()
                .url("baidu.com")
                .question("如何使用Milvus")
                .answer("使用Milvus")
                .isCorrect(true)
                .build()));
    }
    @Test
    public void deleteCollection() {
        vectorService.deleteCollection();
    }
}
