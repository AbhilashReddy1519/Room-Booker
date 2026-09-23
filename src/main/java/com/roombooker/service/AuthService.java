package com.roombooker.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roombooker.dto.AuthResponse;
import com.roombooker.dto.LoginRequest;
import com.roombooker.dto.RegisterRequest;
import com.roombooker.dto.UserResponse;
import com.roombooker.exception.ConflictException;
import com.roombooker.exception.ResourceNotFoundException;
import com.roombooker.repository.UserRepository;
import com.roombooker.security.JwtTokenProvider;
import com.roombooker.users.User;
import com.roombooker.users.UserRole;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email(), "USER_EMAIL_CONFLICT");
        }

        User user = User.builder()
            .name(request.name())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(UserRole.USER)
            .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String token = tokenProvider.generateToken(authentication, savedUser.getId());
        UserResponse userResponse = toUserResponse(savedUser);

        return new AuthResponse(token, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = tokenProvider.generateToken(authentication, user.getId());
        UserResponse userResponse = toUserResponse(user);

        return new AuthResponse(token, userResponse);
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
