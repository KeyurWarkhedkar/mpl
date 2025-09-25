package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDto {
    @NotNull(message = "Status of question submission cannot be null!")
    private String status;

    @NotNull(message = "Team name cannot be null!")
    private String teamName;
}
