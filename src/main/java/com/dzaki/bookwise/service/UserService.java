package com.dzaki.bookwise.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.exception.BadRequestException;
import com.dzaki.bookwise.exception.ResourceAlreadyExistsException;
import com.dzaki.bookwise.exception.ResourceNotFoundException;
import com.dzaki.bookwise.model.users.CreateUserRequest;
import com.dzaki.bookwise.model.users.GetAllUserRequest;
import com.dzaki.bookwise.model.users.UpdateUserRequest;
import com.dzaki.bookwise.model.users.UserResponse;
import com.dzaki.bookwise.repo.UserRepo;
import com.dzaki.bookwise.security.BCrypt;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepo userRepo;

    private final ValidationService validationService;

    private final RefreshTokenService refreshTokenService;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;

        StringBuilder password = new StringBuilder();
        password.append(upper.charAt(secureRandom.nextInt(upper.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        for (int i = 0; i < 6; i++) {
            password.append(all.charAt(secureRandom.nextInt(all.length())));
        }

        // Shuffle to avoid predictable format
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validationService.validate(request);

        String email = request.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User with email " + email + " already exists.");
        }

        String password = request.getPassword();
        boolean generated = false;
        if (password == null || password.isBlank()) {
            password = generateRandomPassword();
            generated = true;
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        userRepo.save(user);
        
        UserResponse response = toUserResponse(user);
        if (generated) {
            response.setGeneratedPassword(password);
        }
        return response;
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return toUserResponse(user);
    }

    @Transactional
    public Page<UserResponse> getAllUser(GetAllUserRequest request) {
        validationService.validate(request);

        Specification<User> spec = (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                String likePattern = "%" + request.getName().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("name")), likePattern));
            }

            if (StringUtils.hasText(request.getEmail())) {
                String likePattern = "%" + request.getEmail().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("email")), likePattern));
            }

            if (StringUtils.hasText(request.getRole())) {
                String likePattern = "%" + request.getRole().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("role")), likePattern));
            }

            if (predicates.isEmpty()) {
                return builder.conjunction();
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // setup sort
        Sort sort = Sort.unsorted();
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(request.getSortDirection())) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<User> user = userRepo.findAll(spec, pageable);
        List<UserResponse> userResponses = user.getContent().stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());

        return new PageImpl<>(userResponses, pageable, user.getTotalElements());
    }

    @Transactional
    public UserResponse resetPassword(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        String newPassword = generateRandomPassword();
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepo.save(user);

        refreshTokenService.revokeAllUserTokens(user);

        UserResponse response = toUserResponse(user);
        response.setGeneratedPassword(newPassword);
        return response;
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        validationService.validate(request);

        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepo.existsByEmailAndIdNot(email, userId)) {
                throw new ResourceAlreadyExistsException("User with email " + email + " already exists");
            }
            user.setEmail(email);
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole());
        }

        userRepo.save(user);

        return toUserResponse(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getIsActive()) {
            throw new BadRequestException("User is already deactivated");
        }

        user.setIsActive(false);
        user.setDeactivatedAt(OffsetDateTime.now());
        refreshTokenService.revokeAllUserTokens(user);
        userRepo.save(user);
    }

    @Transactional
    public void hardDeleteUser(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getIsActive()) {
            throw new BadRequestException("Cannot delete active user. Deactivate first.");
        }

        refreshTokenService.revokeAllUserTokens(user);
        userRepo.delete(user);
    }

    @Transactional
    public void reactivateUser(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getIsActive()) {
            throw new BadRequestException("User is already active");
        }

        user.setIsActive(true);
        user.setDeactivatedAt(null);
        userRepo.save(user);
    }
}
