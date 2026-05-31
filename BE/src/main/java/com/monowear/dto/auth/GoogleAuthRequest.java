package com.monowear.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Google ID Token không được để trống")
        @JsonProperty("idToken")
        String idToken
) {}
