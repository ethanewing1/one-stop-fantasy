package com.onestopfantasy.league;

import com.onestopfantasy.platform.SleeperClient;
import com.onestopfantasy.user.User;
import com.onestopfantasy.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock LeagueRepository leagueRepo;
    @Mock UserRepository userRepo;
    @Mock SleeperClient sleeperClient;
    @InjectMocks LeagueService leagueService;

    private User currentUser;

    @BeforeEach
    void setUpSecurityContext() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("test@example.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getLeagues_returnsLeaguesForCurrentUser() {
        ConnectedLeague league = new ConnectedLeague();
        league.setLeagueName("My League");
        when(leagueRepo.findByUser(currentUser)).thenReturn(List.of(league));

        List<ConnectedLeague> result = leagueService.getLeagues();

        assertEquals(1, result.size());
        assertEquals("My League", result.get(0).getLeagueName());
    }

    @Test
    void connectSleeper_savesNewLeagues() {
        when(sleeperClient.resolveUserId("johndoe")).thenReturn("sleeper123");
        when(sleeperClient.fetchLeagues("sleeper123")).thenReturn(List.of(
                Map.of("league_id", "l1", "name", "Dynasty League"),
                Map.of("league_id", "l2", "name", "Redraft League")
        ));
        when(leagueRepo.existsByUserAndCredentialsContaining(any(), any())).thenReturn(false);
        when(leagueRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ConnectedLeague> saved = leagueService.connectSleeper("johndoe");

        assertEquals(2, saved.size());
        assertEquals("Dynasty League", saved.get(0).getLeagueName());
        assertEquals(Platform.SLEEPER, saved.get(0).getPlatform());
        assertEquals("nfl", saved.get(0).getSport());
        verify(leagueRepo, times(2)).save(any());
    }

    @Test
    void connectSleeper_skipsAlreadyConnectedLeagues() {
        when(sleeperClient.resolveUserId("johndoe")).thenReturn("sleeper123");
        when(sleeperClient.fetchLeagues("sleeper123")).thenReturn(List.of(
                Map.of("league_id", "l1", "name", "Dynasty League"),
                Map.of("league_id", "l2", "name", "Redraft League")
        ));
        when(leagueRepo.existsByUserAndCredentialsContaining(currentUser, "l1")).thenReturn(true);
        when(leagueRepo.existsByUserAndCredentialsContaining(currentUser, "l2")).thenReturn(false);
        when(leagueRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ConnectedLeague> saved = leagueService.connectSleeper("johndoe");

        assertEquals(1, saved.size());
        assertEquals("Redraft League", saved.get(0).getLeagueName());
        verify(leagueRepo, times(1)).save(any());
    }

    @Test
    void connectSleeper_noLeagues_returnsEmptyList() {
        when(sleeperClient.resolveUserId("johndoe")).thenReturn("sleeper123");
        when(sleeperClient.fetchLeagues("sleeper123")).thenReturn(List.of());

        assertTrue(leagueService.connectSleeper("johndoe").isEmpty());
        verify(leagueRepo, never()).save(any());
    }

    @Test
    void deleteLeague_ownLeague_deletesIt() {
        UUID leagueId = UUID.randomUUID();
        ConnectedLeague league = new ConnectedLeague();
        league.setId(leagueId);
        league.setUser(currentUser);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));

        leagueService.deleteLeague(leagueId);

        verify(leagueRepo).delete(league);
    }

    @Test
    void deleteLeague_notFound_throwsNotFound() {
        UUID leagueId = UUID.randomUUID();
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> leagueService.deleteLeague(leagueId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteLeague_notOwner_throwsForbidden() {
        UUID leagueId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        ConnectedLeague league = new ConnectedLeague();
        league.setId(leagueId);
        league.setUser(otherUser);
        when(leagueRepo.findById(leagueId)).thenReturn(Optional.of(league));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> leagueService.deleteLeague(leagueId));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(leagueRepo, never()).delete(any());
    }
}
