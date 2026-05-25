package com.onestopfantasy.auth;

import com.onestopfantasy.user.User;
import com.onestopfantasy.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder encoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass")).thenReturn("hashed");
        when(jwtUtil.generateToken("a@b.com")).thenReturn("jwt-token");

        String token = authService.register("a@b.com", "pass");

        assertEquals("jwt-token", token);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertEquals("a@b.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPasswordHash());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepo.findByEmail("dup@b.com")).thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register("dup@b.com", "pass"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPasswordHash("hashed");
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("a@b.com")).thenReturn("jwt-token");

        assertEquals("jwt-token", authService.login("a@b.com", "pass"));
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(userRepo.findByEmail("x@b.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("x@b.com", "pass"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPasswordHash("hashed");
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login("a@b.com", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void loadUserByUsername_returnsUserDetailsWithRoleUser() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPasswordHash("hashed");
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        UserDetails details = authService.loadUserByUsername("a@b.com");

        assertEquals("a@b.com", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepo.findByEmail("x@b.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("x@b.com"));
    }
}
