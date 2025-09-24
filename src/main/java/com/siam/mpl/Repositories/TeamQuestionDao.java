package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.TeamQuestion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamQuestionDao extends JpaRepository<TeamQuestion, Integer> {
    @Query("select tq from TeamQuestion tq where tq.team.id = ?1")
    public Optional<TeamQuestion> findByTeamId(int teamId);

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    //@Query("select tq from TeamQuestion tq where tq.question.id = ?1")
    public Optional<TeamQuestion> findByQuestionId(String questionId);
}
