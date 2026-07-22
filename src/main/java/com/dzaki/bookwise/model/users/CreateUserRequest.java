package com.dzaki.bookwise.model.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    
    @NotBlank(message = "Name is required")
    @Schema(example = "Jane Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(example = "jane@example.com")
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|USER)$", message = "Role must be ADMIN or USER")
    @Schema(example = "USER")
    private String role;

    @Schema(example = "Str0ng!Pass", description = "Optional. Auto-generated if not provided")
    private String password;
}
