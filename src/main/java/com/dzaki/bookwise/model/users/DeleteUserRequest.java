package com.dzaki.bookwise.model.users;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteUserRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
}
