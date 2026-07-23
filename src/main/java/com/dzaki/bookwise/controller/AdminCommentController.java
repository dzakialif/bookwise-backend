package com.dzaki.bookwise.controller;

import com.dzaki.bookwise.model.PagingResponse;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.comment.CommentResponse;
import com.dzaki.bookwise.model.comment.CreateCommentRequest;
import com.dzaki.bookwise.model.comment.GetAllCommentRequest;
import com.dzaki.bookwise.model.comment.UpdateCommentRequest;
import com.dzaki.bookwise.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return WebResponse.<CommentResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            path = "/{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CommentResponse> getCommentById(@PathVariable UUID commentId) {
        CommentResponse response = commentService.getCommentById(commentId);
        return WebResponse.<CommentResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CommentResponse>> getAllComments(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        GetAllCommentRequest request = GetAllCommentRequest.builder()
                .userId(userId)
                .bookId(bookId)
                .content(content)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<CommentResponse> response = commentService.getAllComment(request);

        return WebResponse.<List<CommentResponse>>builder()
                .data(response.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(response.getNumber())
                        .totalPage(response.getTotalPages())
                        .size(response.getSize())
                        .totalItems((int) response.getTotalElements())
                        .build())
                .build();
    }

    @PutMapping(
            path = "/{commentId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<CommentResponse> updateComment(@PathVariable UUID commentId, @Valid @RequestBody UpdateCommentRequest request) {
        CommentResponse response = commentService.updateComment(commentId, request);
        return WebResponse.<CommentResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping(
            path = "/{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return WebResponse.<Void>builder()
                .message("Comment deleted")
                .build();
    }
}
