package com.dzaki.bookwise.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dzaki.bookwise.entity.RefreshToken;
import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.UnauthorizedException;
import com.dzaki.bookwise.repo.RefreshTokenRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepo refreshTokenRepo;

    @Value("${bookwise.refresh-token.expiry-days:30}")
    private int expiryDays;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateRawToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(expiryDays));

        refreshTokenRepo.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepo.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            refreshToken.setRevokedAt(OffsetDateTime.now());
            refreshTokenRepo.save(refreshToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepo.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepo.findByUser_Id(user.getId());
        for (RefreshToken token : activeTokens) {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(OffsetDateTime.now());
            }
        }
        refreshTokenRepo.saveAll(activeTokens);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        List<RefreshToken> allTokens = refreshTokenRepo.findAll();
        for (RefreshToken token : allTokens) {
            if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
                refreshTokenRepo.delete(token);
            }
        }
    }
}
