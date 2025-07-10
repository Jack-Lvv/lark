package com.cqupt.lark.vector.mapper;

import com.cqupt.lark.vector.model.entity.QaRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaRecordMapper {
    void insert(QaRecord record);

    QaRecord getByVectorId(String vectorId);

    void wrongQaRecord(String vectorId);
}
