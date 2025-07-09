package com.cqupt.lark.rag.repository;

import com.cqupt.lark.rag.model.QaRecord;

import java.util.Collection;
import java.util.List;

public interface RAGRepository {

    void save(QaRecord record);

    List<QaRecord> findByVectorIdIn(String[] vectorIds);
}
