package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.Teams;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamDao extends JpaRepository<Teams, Integer> {
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<Teams> findByTeamName(String teamName);

    public List<Teams> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Teams t where t.teamName = ?1")
    public Optional<Teams> findByTeamNameWithLock(String teamName);
}
