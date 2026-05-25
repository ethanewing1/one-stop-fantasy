package com.onestopfantasy.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleeperClientTest {

    @Mock RestTemplate restTemplate;
    @InjectMocks SleeperClient sleeperClient;

    @Test
    void getCurrentSeason_returnsSeason() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/state/nfl", Map.class))
                .thenReturn(Map.of("season", "2025"));

        assertEquals("2025", sleeperClient.getCurrentSeason());
    }

    @Test
    void getCurrentSeason_nullResponse_throwsBadGateway() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/state/nfl", Map.class))
                .thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sleeperClient.getCurrentSeason());

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void resolveUserId_validUsername_returnsUserId() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/user/johndoe", Map.class))
                .thenReturn(Map.of("user_id", "abc123", "username", "johndoe"));

        assertEquals("abc123", sleeperClient.resolveUserId("johndoe"));
    }

    @Test
    void resolveUserId_unknownUsername_throwsNotFound() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/user/ghost", Map.class))
                .thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sleeperClient.resolveUserId("ghost"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void fetchLeagues_returnsLeagues() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/state/nfl", Map.class))
                .thenReturn(Map.of("season", "2025"));
        when(restTemplate.getForObject("https://api.sleeper.app/v1/user/abc123/leagues/nfl/2025", Map[].class))
                .thenReturn(new Map[]{
                        Map.of("league_id", "l1", "name", "Dynasty League"),
                        Map.of("league_id", "l2", "name", "Redraft League")
                });

        List<Map<String, Object>> leagues = sleeperClient.fetchLeagues("abc123");

        assertEquals(2, leagues.size());
        assertEquals("Dynasty League", leagues.get(0).get("name"));
        assertEquals("Redraft League", leagues.get(1).get("name"));
    }

    @Test
    void fetchLeagues_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject("https://api.sleeper.app/v1/state/nfl", Map.class))
                .thenReturn(Map.of("season", "2025"));
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(null);

        assertTrue(sleeperClient.fetchLeagues("abc123").isEmpty());
    }
}
