package com.dzaki.bookwise.model.users;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAllUserRequest {
    
    private String name;

    private String email;

    @Pattern(regexp = "^(ADMIN|USER)$", message = "Role must be ADMIN or USER")
    private String role;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    private String sortBy;

    private String sortDirection;
}
