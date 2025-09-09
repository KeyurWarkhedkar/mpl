package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Enums.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MysteryQuestionDao extends JpaRepository<MysteryQuestion, Integer> {
    public List<MysteryQuestion> findByDifficultyAndQuestionStatus(String difficulty, QuestionStatus questionStatus);
}
