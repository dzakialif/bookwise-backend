package com.dzaki.bookwise.model.rating;

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
public class GetAllRatingRequest {
    
    private UUID userId;

    private UUID bookId;

    private Integer rating;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    private String sortBy;

    private String sortDirection;
}
