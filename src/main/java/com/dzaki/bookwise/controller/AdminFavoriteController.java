package com.dzaki.bookwise.controller;

import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.favorite.CreateFavoriteRequest;
import com.dzaki.bookwise.model.favorite.FavoriteResponse;
import com.dzaki.bookwise.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/favorites")
@RequiredArgsConstructor
public class AdminFavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<FavoriteResponse> createFavorite(@Valid @RequestBody CreateFavoriteRequest request) {
        FavoriteResponse response = favoriteService.createFavorite(request);
        return WebResponse.<FavoriteResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping(
            path = "/{favoriteId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> deleteFavorite(@PathVariable UUID favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
        return WebResponse.<Void>builder()
                .message("Favorite deleted")
                .build();
    }
}
