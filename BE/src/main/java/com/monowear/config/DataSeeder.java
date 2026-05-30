package com.monowear.config;

import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.service.AuthService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Khởi tạo dữ liệu mặc định khi server start (dev mode).
 * Fix lại password admin vì hash trong migration SQL không khớp với Elytron
 * BCrypt.
 */
@ApplicationScoped
public class DataSeeder {
 
 
    private static final Logger LOG = Logger.getLogger(DataSeeder.class);

    @Inject
    AuthService authService;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        // 1. Cập nhật mật khẩu cho tài khoản Admin
        User admin = User.find("email", "admin@monowear.io").firstResult();
        if (admin != null) {
            admin.passwordHash = authService.hashPasswordPublic("Admin@123");
            LOG.info("Admin password has been re-synced on startup.");
        }

        // 2. Tạo tài khoản Staff mặc định nếu chưa tồn tại
        String staffEmail = "staff@monowear.io";
        User staff = User.find("email", staffEmail).firstResult();

        if (staff == null) {
            staff = new User();
            staff.email = staffEmail;
            staff.fullName = "Nhân viên Mono Wear";
            staff.role = UserRole.STAFF;
            staff.passwordHash = authService.hashPasswordPublic("123456");
            staff.isActive = true;

            staff.persist();
            LOG.info("Default staff account created: " + staffEmail + " with password: 123456");
        }
    }
}