package com.cqupt.lark.vector.service;

import com.cqupt.lark.vector.model.entity.QaRecord;

import java.util.List;

public interface VectorService {

    void initMilvusCollection();

    void saveQaRecord(QaRecord record);

    List<QaRecord> searchSimilarTexts(QaRecord query);

    void deleteCollection();
}
