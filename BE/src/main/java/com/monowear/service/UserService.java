package com.monowear.service;

import com.monowear.dto.auth.CreateStaffRequest;
import com.monowear.dto.auth.UserResponse;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    AuthService authService;

    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        if (User.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new DuplicateResourceException("Email [" + request.email() + "] đã được sử dụng");
        }

        User staff = new User();
        staff.email = request.email().trim().toLowerCase();
        staff.passwordHash = authService.hashPasswordPublic(request.password());
        staff.role = UserRole.STAFF;
        staff.fullName = request.fullName().trim();
        staff.phoneNumber = request.phoneNumber();
        staff.isActive = true;
        staff.persist();

        LOG.infof("Staff created: %s (ID: %d)", staff.email, staff.id);
        return UserResponse.from(staff);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Tài khoản", id);
        }
        if (user.role == UserRole.ADMIN) {
            throw new BadRequestException("Không thể xóa tài khoản Admin");
        }
        user.isActive = false;
        LOG.infof("User deactivated: %s (ID: %d)", user.email, user.id);
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Tài khoản", id);
        }
        user.isActive = true;
        LOG.infof("User activated: %s (ID: %d)", user.email, user.id);
        return UserResponse.from(user);
    }
}
