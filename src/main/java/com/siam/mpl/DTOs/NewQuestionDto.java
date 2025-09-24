package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewQuestionDto {
    @NotNull(message="Question Id cannot be null")
    private String questionId;

    @NotNull(message="Question content cannot be null")
    private String question;
}
