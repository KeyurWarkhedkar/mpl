package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateDto {
    @NotNull(message="Question content cannot be null")
    private String question;
}
