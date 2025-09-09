package com.siam.mpl.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Teams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Team name is required!")
    private String teamName;

    private int points;

    @OneToOne
    @JoinColumn(name = "mystery_question")
    private MysteryQuestion mysteryQuestion;

    private LocalDateTime startTime;
}
