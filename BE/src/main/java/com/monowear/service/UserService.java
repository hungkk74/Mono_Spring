package com.monowear.service;

import com.monowear.dto.auth.CreateStaffRequest;
import com.monowear.dto.auth.UserResponse;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        if (userRepository.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new DuplicateResourceException("Email [" + request.email() + "] đã được sử dụng");
        }

        User staff = new User();
        staff.setEmail(request.email().trim().toLowerCase());
        staff.setPasswordHash(authService.hashPasswordPublic(request.password()));
        staff.setRole(UserRole.STAFF);
        staff.setFullName(request.fullName().trim());
        staff.setPhoneNumber(request.phoneNumber());
        staff.setIsActive(true);
        userRepository.save(staff);

        log.info("Staff created: {} (ID: {})", staff.getEmail(), staff.getId());
        return UserResponse.from(staff);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Không thể xóa tài khoản Admin");
        }
        user.setIsActive(false);
        log.info("User deactivated: {} (ID: {})", user.getEmail(), user.getId());
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));
        user.setIsActive(true);
        log.info("User activated: {} (ID: {})", user.getEmail(), user.getId());
        return UserResponse.from(user);
    }
}
