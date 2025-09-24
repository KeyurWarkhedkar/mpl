package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.Question;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionDao extends JpaRepository<Question, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Question q where q.id = ?1")
    public Optional<Question> findByIdWithLock(String id);
}
