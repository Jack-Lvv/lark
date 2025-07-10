package com.cqupt.lark.vector.service;


import com.cqupt.lark.vector.model.dto.AnswerDTO;

public interface SearchVectorService {

    AnswerDTO searchAnswer(String url, String question);

    void addQaRecord(String url, String question, String answer, boolean isCorrect);

    void wrongQaRecord(String vectorId);
}
