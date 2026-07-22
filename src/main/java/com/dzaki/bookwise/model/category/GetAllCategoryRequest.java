package com.dzaki.bookwise.model.category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllCategoryRequest {
    
    private String name;

    private String description;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    private String sortBy;

    private String sortDirection;
}
