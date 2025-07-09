package com.cqupt.lark.rag;

import com.cqupt.lark.rag.model.QaRecord;
import com.cqupt.lark.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RagTest {

    @Autowired
    private RagService ragService;
    @Test
    public void add() {
        ragService.initMilvusCollection();
        ragService.saveQaRecord(QaRecord.builder()
                        .url("baidu.com")
                        .question("如何使用Milvus")
                        .answer("使用Milvus")
                        .isCorrect(true)
                        .build());
    }

    @Test
    public void query() {
        System.out.println(ragService.searchSimilarTexts(QaRecord.builder()
                .url("baidu.com")
                .question("如何使用Milvus")
                .answer("使用Milvus")
                .isCorrect(true)
                .build()));
    }
    @Test
    public void deleteCollection() {
        ragService.deleteCollection();
    }
}
