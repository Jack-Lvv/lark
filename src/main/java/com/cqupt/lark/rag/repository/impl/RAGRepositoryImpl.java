package com.cqupt.lark.rag.repository.impl;

import com.cqupt.lark.rag.mapper.QaRecordMapper;
import com.cqupt.lark.rag.model.QaRecord;
import com.cqupt.lark.rag.repository.RAGRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RAGRepositoryImpl implements RAGRepository {

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

}
