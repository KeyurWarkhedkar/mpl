package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionDao extends JpaRepository<Question, String> {
}
