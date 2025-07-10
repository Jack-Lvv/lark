package com.cqupt.lark.vector.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaRecord {

    private Long id;
    private String url;
    private String question;
    private String answer;
    private boolean isCorrect;
    private String vectorId;

    public String toStringForVector() {
        return "url='" + url + '\'' +
                ", question='" + question + '\'';
    }

}
