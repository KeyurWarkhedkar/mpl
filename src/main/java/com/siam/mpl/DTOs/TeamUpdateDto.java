package com.siam.mpl.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamUpdateDto {
    private String teamName;
    private Integer points;
    private Integer mysteryQuestionId;
}
