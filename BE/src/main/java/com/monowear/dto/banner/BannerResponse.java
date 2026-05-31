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
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getMediaUrl(),
                banner.getMediaType() != null ? banner.getMediaType().name() : "IMAGE",
                banner.getThumbnailUrl(),
                banner.getLinkUrl(),
                banner.getCtaText(),
                banner.getDisplayOrder(),
                banner.getIsActive(),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
