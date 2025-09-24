package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Enums.QuestionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface MysteryQuestionDao extends JpaRepository<MysteryQuestion, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<MysteryQuestion> findByDifficultyAndQuestionStatus(String difficulty, QuestionStatus questionStatus);
}
