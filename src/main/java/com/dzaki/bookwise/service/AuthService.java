package com.dzaki.bookwise.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dzaki.bookwise.entity.RefreshToken;
import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.ResourceAlreadyExistsException;
import com.dzaki.bookwise.exception.UnauthorizedException;
import com.dzaki.bookwise.model.auth.AuthResponse;
import com.dzaki.bookwise.model.auth.LoginRequest;
import com.dzaki.bookwise.model.auth.RefreshTokenResponse;
import com.dzaki.bookwise.model.auth.RegisterRequest;
import com.dzaki.bookwise.model.users.UserResponse;
import com.dzaki.bookwise.repo.UserRepo;
import com.dzaki.bookwise.security.BCrypt;
import com.dzaki.bookwise.security.JWTUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;

    private final RefreshTokenService refreshTokenService;

    private final ValidationService validationService;

    private final JWTUtil jwtUtil;

    @Value("${bookwise.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validationService.validate(request);

        String email = request.getEmail().toLowerCase().trim();

        if (userRepo.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setRole("USER");
        user.setIsActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        userRepo.save(user);

        return toUserResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        validationService.validate(request);

        String email = request.getEmail().toLowerCase().trim();

        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(accessTokenExpiration / 1000)
            .user(toUserResponse(user))
            .rawRefreshToken(rawRefreshToken)
            .build();
    }

    @Transactional
    public RefreshTokenResponse refresh(String rawRefreshToken) {
        RefreshToken token = refreshTokenService.validateRefreshToken(rawRefreshToken);
        User user = token.getUser();

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        return RefreshTokenResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(accessTokenExpiration / 1000)
            .build();
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(
            refreshTokenService.validateRefreshToken(rawRefreshToken)
        );
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
