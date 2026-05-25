package com.onestopfantasy.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    // AuthService also satisfies the UserDetailsService injection point in JwtFilter
    @MockitoBean AuthService authService;
    @MockitoBean JwtUtil jwtUtil;

    @Test
    void register_returns201WithToken() throws Exception {
        when(authService.register("a@b.com", "pass")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"pass\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dup@b.com\",\"password\":\"pass\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200WithToken() throws Exception {
        when(authService.login("a@b.com", "pass")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
