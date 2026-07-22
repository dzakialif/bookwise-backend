package com.dzaki.bookwise.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dzaki.bookwise.model.PagingResponse;
import com.dzaki.bookwise.model.WebResponse;
import com.dzaki.bookwise.model.users.CreateUserRequest;
import com.dzaki.bookwise.model.users.GetAllUserRequest;
import com.dzaki.bookwise.model.users.UpdateUserRequest;
import com.dzaki.bookwise.model.users.UserResponse;
import com.dzaki.bookwise.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;

    @PostMapping(
        produces=MediaType.APPLICATION_JSON_VALUE,
        consumes=MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);

        return WebResponse.<UserResponse>builder()
            .data(response)
            .build();
    }

    @GetMapping(
        path = "/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getUserById(userId);
        
        return WebResponse.<UserResponse>builder()
            .data(response)
            .build();
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<UserResponse>> getAllUsers(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "email", required = false) String email,
        @RequestParam(value = "role", required = false) String role,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
        @RequestParam(value = "sortBy", required = false) String sortBy,
        @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection
    ) {
        GetAllUserRequest request = GetAllUserRequest.builder()
            .page(page)
            .size(size)
            .name(name)
            .email(email)
            .role(role)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

            Page<UserResponse> response = userService.getAllUser(request);

            return WebResponse.<List<UserResponse>>builder().data(response.getContent())
                .paging(PagingResponse.builder()
                    .currentPage(response.getNumber())
                    .totalPage(response.getTotalPages())
                    .size(response.getSize())
                    .totalItems((int) response.getTotalElements())
                    .build())
                .build();
    }

    @PutMapping(
        path = "/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        
        return WebResponse.<UserResponse>builder()
            .data(response)
            .build();
    }

    @PostMapping(
        path = "/{userId}/deactivate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return WebResponse.<Void>builder()
            .message("User deactivated")
            .build();
    }

    @PostMapping(
        path = "/{userId}/reactivate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Void> reactivateUser(@PathVariable UUID userId) {
        userService.reactivateUser(userId);
        return WebResponse.<Void>builder()
            .message("User reactivated")
            .build();
    }

    @PostMapping(
        path = "/{userId}/reset-password",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> resetPassword(@PathVariable UUID userId) {
        UserResponse response = userService.resetPassword(userId);
        return WebResponse.<UserResponse>builder()
            .data(response)
            .message("Password reset successfully")
            .build();
    }

    @DeleteMapping(
        path = "/{userId}/hard",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteUser(@PathVariable UUID userId) {
        userService.hardDeleteUser(userId);
    }
}
