package com.cqupt.lark.vector.repository.impl;

import com.cqupt.lark.vector.mapper.QaRecordMapper;
import com.cqupt.lark.vector.model.entity.QaRecord;
import com.cqupt.lark.vector.repository.VectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VectorRepositoryImpl implements VectorRepository {

    private final QaRecordMapper qaRecordMapper;
    @Override
    public void save(QaRecord record) {
        qaRecordMapper.insert(record);
    }

    @Override
    public List<QaRecord> findByVectorIdIn(String[] vectorIds) {
        List<QaRecord> records = new ArrayList<>();
        for (String vectorId : vectorIds) {
            records.add(qaRecordMapper.getByVectorId(vectorId));
        }
        return records;
    }

    @Override
    public void wrongQaRecord(String vectorId) {
        qaRecordMapper.wrongQaRecord(vectorId);
    }

}
