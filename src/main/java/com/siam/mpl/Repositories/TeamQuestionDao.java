package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.TeamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamQuestionDao extends JpaRepository<TeamQuestion, Integer> {
    public Optional<TeamQuestion> findByTeamId(int teamId);
    public Optional<TeamQuestion> findByQuestionId(String questionId);
}
