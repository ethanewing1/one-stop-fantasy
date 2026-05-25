package com.onestopfantasy.auth;

import com.onestopfantasy.user.User;
import com.onestopfantasy.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService implements UserDetailsService {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtil jwtUtil;

    public String register(String email, String password) {
        if (userRepo.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        userRepo.save(user);
        return jwtUtil.generateToken(email);
    }

    public String login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return jwtUtil.generateToken(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_USER")
                .build();
    }
}
