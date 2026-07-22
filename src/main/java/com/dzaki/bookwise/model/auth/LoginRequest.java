package com.dzaki.bookwise.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Schema(example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "Str0ng!Pass")
    private String password;
}
