package com.onestopfantasy.league;

import com.onestopfantasy.platform.SleeperClient;
import com.onestopfantasy.user.User;
import com.onestopfantasy.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LeagueService {

    @Autowired private LeagueRepository leagueRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SleeperClient sleeperClient;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public List<ConnectedLeague> getLeagues() {
        return leagueRepo.findByUser(currentUser());
    }

    public List<ConnectedLeague> connectSleeper(String username) {
        User user = currentUser();
        String sleeperUserId = sleeperClient.resolveUserId(username);
        List<Map<String, Object>> rawLeagues = sleeperClient.fetchLeagues(sleeperUserId);

        List<ConnectedLeague> saved = new ArrayList<>();
        for (Map<String, Object> raw : rawLeagues) {
            String leagueId = (String) raw.get("league_id");
            if (leagueRepo.existsByUserAndCredentialsContaining(user, leagueId)) continue;

            String credJson = String.format(
                    "{\"username\":\"%s\",\"sleeperUserId\":\"%s\",\"leagueId\":\"%s\"}",
                    username, sleeperUserId, leagueId
            );
            ConnectedLeague cl = new ConnectedLeague();
            cl.setUser(user);
            cl.setPlatform(Platform.SLEEPER);
            cl.setLeagueName((String) raw.get("name"));
            cl.setCredentials(credJson);
            cl.setSport("nfl");
            cl.setLastSyncedAt(Instant.now());
            saved.add(leagueRepo.save(cl));
        }
        return saved;
    }

    public void deleteLeague(UUID id) {
        ConnectedLeague league = leagueRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"));
        if (!league.getUser().getId().equals(currentUser().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your league");
        leagueRepo.delete(league);
    }
}
