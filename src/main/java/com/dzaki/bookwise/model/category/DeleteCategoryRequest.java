package com.dzaki.bookwise.model.category;

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
public class DeleteCategoryRequest {
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
