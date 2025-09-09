package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.TeamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamQuestionDao extends JpaRepository<TeamQuestion, Integer> {
}
