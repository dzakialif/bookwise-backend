package com.dzaki.bookwise.model.favorite;

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
public class GetFavoriteByIdRequest {
    
    @NotNull
    private UUID favoriteId;
}
