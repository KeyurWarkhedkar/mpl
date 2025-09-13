package com.siam.mpl.Entities;

import com.siam.mpl.Enums.QuestionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MysteryQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String question;

    private String difficulty;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;
}
