package com.dzaki.bookwise.model.book;

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
public class DeleteBookRequest {
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
}
