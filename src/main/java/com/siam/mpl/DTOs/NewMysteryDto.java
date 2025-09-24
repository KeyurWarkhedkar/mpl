package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMysteryDto {
    @NotNull(message="Question cannot be null")
    private String question;

    @NotNull(message="Difficulty of the question cannot be null")
    private String difficulty;
}
