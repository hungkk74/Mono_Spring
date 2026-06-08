package com.monowear.dto.auth;

import jakarta.validation.constraints.Size;


public record UpdateProfileRequest(
        @Size(max = 100) String fullName,
        @Size(max = 20) String phoneNumber,
        @Size(max = 500) String address
) {}
