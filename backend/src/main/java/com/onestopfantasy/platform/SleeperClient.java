package com.onestopfantasy.platform;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class SleeperClient {

    private final RestTemplate restTemplate;

    public SleeperClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public String getCurrentSeason() {
        Map<String, Object> state = restTemplate.getForObject("https://api.sleeper.app/v1/state/nfl", Map.class);
        if (state == null)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach Sleeper API");
        return (String) state.get("season");
    }

    @SuppressWarnings("unchecked")
    public String resolveUserId(String username) {
        Map<String, Object> response = restTemplate.getForObject("https://api.sleeper.app/v1/user/" + username, Map.class);
        if (response == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleeper user not found: " + username);
        return (String) response.get("user_id");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Map<String, Object>> fetchLeagues(String sleeperUserId) {
        String season = getCurrentSeason();
        String url = "https://api.sleeper.app/v1/user/" + sleeperUserId + "/leagues/nfl/" + season;
        Map[] leagues = restTemplate.getForObject(url, Map[].class);
        return leagues != null ? List.of(leagues) : List.of();
    }
}
