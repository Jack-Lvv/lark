package com.cqupt.lark.vector.service.impl;

import com.cqupt.lark.vector.model.dto.AnswerDTO;
import com.cqupt.lark.vector.model.entity.QaRecord;
import com.cqupt.lark.vector.repository.VectorRepository;
import com.cqupt.lark.vector.service.SearchVectorService;
import com.cqupt.lark.vector.service.VectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchVectorServiceImpl implements SearchVectorService {

    private final VectorService vectorService;
    private final VectorRepository vectorRepository;
    @Override
    public AnswerDTO searchAnswer(String url, String question) {
        List<QaRecord> qaRecords = vectorService.searchSimilarTexts(QaRecord.builder()
                .url(url)
                .question(question)
                .build());
        for (QaRecord qaRecord : qaRecords) {
            if (qaRecord.isCorrect()) {
                return AnswerDTO.builder()
                        .vectorId(qaRecord.getVectorId())
                        .answer(qaRecord.getAnswer())
                        .build();
            }
        }
        return null;
    }

    @Override
    public void addQaRecord(String url, String question, String answer, boolean isCorrect) {
        vectorService.saveQaRecord(QaRecord.builder()
                .isCorrect(isCorrect)
                .url(url)
                .answer(answer)
                .question(question)
                .build());
    }

    @Override
    public void wrongQaRecord(String vectorId) {
        vectorRepository.wrongQaRecord(vectorId);
    }
}
