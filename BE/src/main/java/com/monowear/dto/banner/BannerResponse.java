package com.monowear.dto.banner;

import com.monowear.entity.Banner;

import java.time.LocalDateTime;

public record BannerResponse(
        Long id,
        String title,
        String subtitle,
        String mediaUrl,
        String mediaType,
        String thumbnailUrl,
        String linkUrl,
        String ctaText,
        Integer displayOrder,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
                banner.id,
                banner.title,
                banner.subtitle,
                banner.mediaUrl,
                banner.mediaType != null ? banner.mediaType.name() : "IMAGE",
                banner.thumbnailUrl,
                banner.linkUrl,
                banner.ctaText,
                banner.displayOrder,
                banner.isActive,
                banner.createdAt,
                banner.updatedAt
        );
    }
}
