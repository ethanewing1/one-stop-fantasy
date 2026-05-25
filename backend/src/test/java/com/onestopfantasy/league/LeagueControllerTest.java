package com.onestopfantasy.league;

import com.onestopfantasy.auth.AuthService;
import com.onestopfantasy.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeagueController.class)
class LeagueControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean LeagueService leagueService;
    @MockitoBean AuthService authService;
    @MockitoBean JwtUtil jwtUtil;

    private ConnectedLeague makeLeague(String name) {
        ConnectedLeague l = new ConnectedLeague();
        l.setId(UUID.randomUUID());
        l.setLeagueName(name);
        l.setPlatform(Platform.SLEEPER);
        l.setSport("nfl");
        return l;
    }

    @Test
    @WithMockUser
    void list_authenticated_returns200WithLeagues() throws Exception {
        when(leagueService.getLeagues()).thenReturn(List.of(makeLeague("Dynasty League")));

        mockMvc.perform(get("/api/leagues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leagueName").value("Dynasty League"))
                .andExpect(jsonPath("$[0].platform").value("SLEEPER"));
    }

    @Test
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/leagues"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void connectSleeper_validUsername_returns201WithLeagues() throws Exception {
        when(leagueService.connectSleeper("johndoe"))
                .thenReturn(List.of(makeLeague("My League")));

        mockMvc.perform(post("/api/leagues/connect/sleeper")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"johndoe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].leagueName").value("My League"));
    }

    @Test
    @WithMockUser
    void connectSleeper_unknownUsername_returns404() throws Exception {
        when(leagueService.connectSleeper(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleeper user not found"));

        mockMvc.perform(post("/api/leagues/connect/sleeper")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_validId_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(leagueService).deleteLeague(id);

        mockMvc.perform(delete("/api/leagues/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_notOwner_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your league"))
                .when(leagueService).deleteLeague(id);

        mockMvc.perform(delete("/api/leagues/" + id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void delete_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found"))
                .when(leagueService).deleteLeague(id);

        mockMvc.perform(delete("/api/leagues/" + id))
                .andExpect(status().isNotFound());
    }
}
