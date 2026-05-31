package com.monowear.dto.catalog;

import com.monowear.entity.Category;
import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean isActive,
        Long parentId,
        String parentName,
        List<CategoryResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIsActive(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public static CategoryResponse withChildren(Category category) {
        List<CategoryResponse> childList = null;
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            childList = category.getChildren().stream()
                    .filter(c -> c.getIsActive())
                    .map(CategoryResponse::withChildren)
                    .toList();
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIsActive(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                childList,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
