package com.siam.mpl.DTOs;

import com.siam.mpl.Enums.MysteryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MysteryCompletionDto {
    private String teamName;
    private MysteryStatus mysteryCompletionStatus;
}
