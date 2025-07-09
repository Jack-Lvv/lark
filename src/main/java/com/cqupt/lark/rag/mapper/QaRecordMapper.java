package com.cqupt.lark.rag.mapper;

import com.cqupt.lark.rag.model.QaRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaRecordMapper {
    void insert(QaRecord record);

    QaRecord getByVectorId(String vectorId);
}
