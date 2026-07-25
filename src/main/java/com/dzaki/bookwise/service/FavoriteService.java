package com.dzaki.bookwise.service;

import com.dzaki.bookwise.entity.Book;
import com.dzaki.bookwise.entity.Favorite;
import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.favorite.CreateFavoriteRequest;
import com.dzaki.bookwise.model.favorite.FavoriteResponse;
import com.dzaki.bookwise.repo.BookRepo;
import com.dzaki.bookwise.repo.FavoriteRepo;
import com.dzaki.bookwise.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepo favoriteRepo;
    private final UserRepo userRepo;
    private final BookRepo bookRepo;
    private final ValidationService validationService;

    private FavoriteResponse toFavoriteResponse(Favorite favorite) {
        return FavoriteResponse.builder()
                .favoriteId(favorite.getFavoriteId())
                .userId(favorite.getUser().getId())
                .bookId(favorite.getBook().getBookId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    @Transactional
    public FavoriteResponse createFavorite(CreateFavoriteRequest request) {
        validationService.validate(request);

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Book book = bookRepo.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);
        favorite.setCreatedAt(OffsetDateTime.now());

        favoriteRepo.save(favorite);
        return toFavoriteResponse(favorite);
    }

    @Transactional
    public void deleteFavorite(UUID favoriteId) {
        Favorite favorite = favoriteRepo.findById(favoriteId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        favoriteRepo.delete(favorite);
    }
}
