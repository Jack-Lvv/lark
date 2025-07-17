package com.cqupt.lark.vector.repository;

import com.cqupt.lark.vector.model.entity.QaRecord;

import java.util.List;

public interface VectorRepository {

    void save(QaRecord record);

    List<QaRecord> findByVectorIdIn(String[] vectorIds);

    void wrongQaRecord(String vectorId);

    void cleanUselessVectors();
}
