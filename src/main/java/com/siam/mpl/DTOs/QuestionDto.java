package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionDto {
    @NotNull(message="Team Name cannot be null")
    private String teamName;

    @NotNull(message="Question id cannot be null")
    private String questionId;
}
