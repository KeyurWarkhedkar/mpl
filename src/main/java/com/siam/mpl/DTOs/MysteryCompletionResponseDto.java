package com.siam.mpl.DTOs;

import lombok.*;

import java.time.Duration;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class MysteryCompletionResponseDto {
    private long updatedTime;
    private int updatedPoints;
}
