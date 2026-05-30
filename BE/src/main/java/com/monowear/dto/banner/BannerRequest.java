package com.monowear.dto.banner;

import com.monowear.entity.enums.BannerMediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BannerRequest(
        @NotBlank(message = "Tiêu đề không được để trống")
        String title,

        String subtitle,

        @NotBlank(message = "URL media không được để trống")
        String mediaUrl,

        @NotNull(message = "Loại media không được để trống")
        BannerMediaType mediaType,

        String thumbnailUrl,
        String linkUrl,
        String ctaText,
        Integer displayOrder
) {}
