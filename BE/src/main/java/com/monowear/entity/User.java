package com.monowear.entity;

import com.monowear.entity.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "users")
public class User extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public UserRole role = UserRole.CUSTOMER;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "phone_number", length = 20)
    public String phoneNumber;

    @Column(columnDefinition = "TEXT")
    public String address;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // --- Lifecycle Callbacks ---

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Query Methods (Active Record Pattern) ---

    public static Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public static long countByRole(UserRole role) {
        return count("role", role);
    }
}
