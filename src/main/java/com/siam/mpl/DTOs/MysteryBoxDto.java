package com.siam.mpl.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MysteryBoxDto {
    @NotNull(message="Team Name cannot be null")
    private String teamName;

    @NotNull(message="Difficulty of the question cannot be null")
    private String difficulty;

    @NotNull(message="Points deducted cannot be null")
    private int pointsDeducted;
}