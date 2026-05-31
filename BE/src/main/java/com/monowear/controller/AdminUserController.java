package com.monowear.controller;

import com.monowear.dto.auth.CreateStaffRequest;
import com.monowear.dto.auth.UserResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.repository.UserRepository;
import com.monowear.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> result = userRepository.findAll(PageRequest.of(page, size));
        List<UserResponse> items = result.getContent().stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", items,
                "page", page,
                "size", size,
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        )));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<UserResponse>> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo nhân viên thành công", userService.createStaff(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt tài khoản thành công", userService.activateUser(id)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserStats() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "totalCustomers", userRepository.countByRole(UserRole.CUSTOMER),
                "totalStaff", userRepository.countByRole(UserRole.STAFF),
                "totalAdmin", userRepository.countByRole(UserRole.ADMIN)
        )));
    }
}
