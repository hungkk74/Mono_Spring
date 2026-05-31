package com.monowear.service;

import com.monowear.dto.catalog.CategoryRequest;
import com.monowear.dto.catalog.CategoryResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Category;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private final CategoryRepository categoryRepository;

    // ==================== ADMIN/STAFF ====================

    public PagedResponse<CategoryResponse> listAll(int page, int size) {
        Page<Category> result = categoryRepository.findAll(PageRequest.of(page, size));
        List<CategoryResponse> items = result.getContent().stream()
                .map(CategoryResponse::from)
                .toList();
        return PagedResponse.of(items, page, size, result.getTotalElements());
    }

    public CategoryResponse getById(Long id) {
        Category category = findOrThrow(id);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = generateUniqueSlug(request.name(), categoryRepository::findBySlug);

        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setIsActive(true);

        if (request.parentId() != null) {
            Category parent = findOrThrow(request.parentId());
            category.setParent(parent);
        }

        categoryRepository.save(category);
        log.info("Category created: {} (ID: {})", category.getName(), category.getId());
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);

        String newSlug = generateUniqueSlug(request.name(), slug -> {
            var existing = categoryRepository.findBySlug(slug);
            return existing.filter(c -> !c.getId().equals(id));
        });

        category.setName(request.name().trim());
        category.setSlug(newSlug);
        category.setDescription(request.description());

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BadRequestException("Danh mục không thể là parent của chính nó");
            }
            Category parent = findOrThrow(request.parentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        log.info("Category updated: {} (ID: {})", category.getName(), category.getId());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        category.setIsActive(false);
        log.info("Category soft-deleted: {} (ID: {})", category.getName(), category.getId());
    }

    // ==================== PUBLIC ====================

    public List<CategoryResponse> getActiveTree() {
        List<Category> roots = categoryRepository.findRootCategories();
        return roots.stream()
                .map(CategoryResponse::withChildren)
                .toList();
    }

    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", slug));
        return CategoryResponse.withChildren(category);
    }

    // ==================== HELPERS ====================

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", id));
    }

    static String generateSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD);
        String noDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        noDiacritics = noDiacritics.replace("đ", "d");
        String slug = noDiacritics.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    private String generateUniqueSlug(String name, java.util.function.Function<String, java.util.Optional<Category>> finder) {
        String baseSlug = generateSlug(name);
        String slug = baseSlug;
        int counter = 1;
        while (finder.apply(slug).isPresent()) {
            slug = baseSlug + "-" + (++counter);
        }
        return slug;
    }
}
