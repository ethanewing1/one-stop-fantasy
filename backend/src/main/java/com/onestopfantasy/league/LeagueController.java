package com.onestopfantasy.league;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    @Autowired private LeagueService leagueService;

    record ConnectSleeperRequest(String username) {}

    @GetMapping
    public ResponseEntity<List<ConnectedLeague>> list() {
        return ResponseEntity.ok(leagueService.getLeagues());
    }

    @PostMapping("/connect/sleeper")
    public ResponseEntity<List<ConnectedLeague>> connectSleeper(@RequestBody ConnectSleeperRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leagueService.connectSleeper(req.username()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        leagueService.deleteLeague(id);
        return ResponseEntity.noContent().build();
    }
}
