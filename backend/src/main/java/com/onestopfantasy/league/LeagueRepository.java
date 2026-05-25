package com.onestopfantasy.league;

import com.onestopfantasy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LeagueRepository extends JpaRepository<ConnectedLeague, UUID> {
    List<ConnectedLeague> findByUser(User user);
    boolean existsByUserAndCredentialsContaining(User user, String substring);
}
