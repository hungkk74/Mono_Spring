package com.monowear.dto.auth;

import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin user (KHÔNG BAO GIỜ chứa password).
 */
public record UserResponse(
        Long id,
        String email,
        UserRole role,
        String fullName,
        String phoneNumber,
        String address,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
