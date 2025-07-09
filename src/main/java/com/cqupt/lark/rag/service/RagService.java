package com.cqupt.lark.rag.service;

import com.cqupt.lark.rag.model.QaRecord;

import java.util.List;

public interface RagService {

    void initMilvusCollection();

    void saveQaRecord(QaRecord record);

    List<String> searchSimilarTexts(QaRecord query);

    void deleteCollection();
}
