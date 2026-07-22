package com.dzaki.bookwise.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dzaki.bookwise.exception.UnauthorizedException;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.auth.AuthResponse;
import com.dzaki.bookwise.model.auth.LoginRequest;
import com.dzaki.bookwise.model.auth.RefreshTokenResponse;
import com.dzaki.bookwise.model.auth.RegisterRequest;
import com.dzaki.bookwise.model.users.UserResponse;
import com.dzaki.bookwise.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${bookwise.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${bookwise.refresh-token.expiry-days:30}")
    private int refreshTokenExpiryDays;

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
            .httpOnly(true)
            .secure(false)
            .path("/api/v1/auth")
            .maxAge(maxAgeSeconds)
            .sameSite("Strict")
            .build();
    }

    @PostMapping(
        path = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);

        return ResponseEntity.status(201)
            .body(WebResponse.<UserResponse>builder()
                .data(userResponse)
                .message("Registration successful")
                .build());
    }

    @PostMapping(
        path = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authData = authService.login(request);

        String refreshTokenValue = authData.getRawRefreshToken();
        long maxAgeSeconds = (long) refreshTokenExpiryDays * 24 * 60 * 60;
        ResponseCookie cookie = buildRefreshCookie(refreshTokenValue, maxAgeSeconds);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(WebResponse.<AuthResponse>builder()
                .data(authData)
                .build());
    }

    @PostMapping(
        path = "/refresh",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<RefreshTokenResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Refresh token not found");
        }

        RefreshTokenResponse tokenData = authService.refresh(refreshTokenValue);

        return ResponseEntity.ok()
            .body(WebResponse.<RefreshTokenResponse>builder()
                .data(tokenData)
                .message("Token refreshed")
                .build());
    }

    @PostMapping(
        path = "/logout",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Refresh token not found");
        }

        authService.logout(refreshTokenValue);

        ResponseCookie clearCookie = buildRefreshCookie("", 0);

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .body(WebResponse.<Void>builder()
                .message("Logged out successfully")
                .build());
    }
}
