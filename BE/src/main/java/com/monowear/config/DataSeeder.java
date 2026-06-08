package com.monowear.config;

import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.repository.UserRepository;
import com.monowear.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        userRepository.findByEmail("admin@monowear.io").ifPresent(admin -> {
            admin.setPasswordHash(authService.hashPasswordPublic("Admin@123"));
            log.info("Admin password has been re-synced on startup.");
        });


        String staffEmail = "staff@monowear.io";
        if (userRepository.findByEmail(staffEmail).isEmpty()) {
            User staff = new User();
            staff.setEmail(staffEmail);
            staff.setFullName("Nhân viên Mono Wear");
            staff.setRole(UserRole.STAFF);
            staff.setPasswordHash(authService.hashPasswordPublic("123456"));
            staff.setIsActive(true);
            userRepository.save(staff);
            log.info("Default staff account created: {} with password: 123456", staffEmail);
        }
    }
}