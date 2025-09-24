package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamUpdateDto {
    @NotNull(message="Team Name cannot be null")
    private String teamName;

    @NotNull(message="Points to be updated cannot be null")
    private Integer points;

    /*@NotNull(message="Question id cannot be null")
    private Integer mysteryQuestionId;*/
}
