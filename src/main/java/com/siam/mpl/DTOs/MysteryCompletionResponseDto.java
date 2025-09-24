package com.siam.mpl.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MysteryCompletionResponseDto {
    private Duration updatedTime;
    private int updatedPoints;
}
