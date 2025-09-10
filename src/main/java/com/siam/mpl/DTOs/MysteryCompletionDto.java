package com.siam.mpl.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siam.mpl.Enums.MysteryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MysteryCompletionDto {
    private String teamName;
    @JsonProperty("mysteryCompletionStatus")
    private MysteryStatus mysteryCompletionStatus;
}
