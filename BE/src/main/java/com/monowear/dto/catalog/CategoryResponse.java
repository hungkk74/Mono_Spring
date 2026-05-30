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
    /**
     * Mapping flat (không load children).
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.id,
                category.name,
                category.slug,
                category.description,
                category.isActive,
                category.parent != null ? category.parent.id : null,
                category.parent != null ? category.parent.name : null,
                null,
                category.createdAt,
                category.updatedAt
        );
    }

    /**
     * Mapping kèm children (tree structure).
     */
    public static CategoryResponse withChildren(Category category) {
        List<CategoryResponse> childList = null;
        if (category.children != null && !category.children.isEmpty()) {
            childList = category.children.stream()
                    .filter(c -> c.isActive)
                    .map(CategoryResponse::withChildren)
                    .toList();
        }
        return new CategoryResponse(
                category.id,
                category.name,
                category.slug,
                category.description,
                category.isActive,
                category.parent != null ? category.parent.id : null,
                category.parent != null ? category.parent.name : null,
                childList,
                category.createdAt,
                category.updatedAt
        );
    }
}
