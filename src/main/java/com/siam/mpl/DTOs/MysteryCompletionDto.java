package com.siam.mpl.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siam.mpl.Enums.MysteryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MysteryCompletionDto {
    @NotNull(message="Team Name cannot be null")
    private String teamName;

    @JsonProperty("mysteryCompletionStatus")
    private MysteryStatus mysteryCompletionStatus;
}
