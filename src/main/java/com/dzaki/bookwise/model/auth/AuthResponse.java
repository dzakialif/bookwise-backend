package com.dzaki.bookwise.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dzaki.bookwise.model.users.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private UserResponse user;

    @JsonIgnore
    private String rawRefreshToken;
}
