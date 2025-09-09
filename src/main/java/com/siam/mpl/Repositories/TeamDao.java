package com.siam.mpl.Repositories;

import com.siam.mpl.Entities.Teams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamDao extends JpaRepository<Teams, Integer> {
    public Optional<Teams> findByTeamName(String teamName);
}
