package com.monowear.service;

import com.monowear.dto.catalog.CategoryRequest;
import com.monowear.dto.catalog.CategoryResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Category;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@ApplicationScoped
public class CategoryService {

    private static final Logger LOG = Logger.getLogger(CategoryService.class);
    // Compile once — Pattern.compile() tốn ~100-500µs mỗi lần
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    // ==================== ADMIN/STAFF ====================

    /**
     * Lấy danh sách danh mục (phân trang, bao gồm cả inactive).
     */
    public PagedResponse<CategoryResponse> listAll(int page, int size) {
        var query = Category.findAll();
        long total = query.count();
        List<CategoryResponse> items = query
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(e -> CategoryResponse.from((Category) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    /**
     * Lấy chi tiết danh mục theo ID.
     */
    public CategoryResponse getById(Long id) {
        Category category = findOrThrow(id);
        return CategoryResponse.from(category);
    }

    /**
     * Tạo danh mục mới.
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = generateUniqueSlug(request.name(), Category::findBySlug);

        Category category = new Category();
        category.name = request.name().trim();
        category.slug = slug;
        category.description = request.description();
        category.isActive = true;

        // Set parent if provided
        if (request.parentId() != null) {
            Category parent = findOrThrow(request.parentId());
            category.parent = parent;
        }

        category.persist();
        LOG.infof("Category created: %s (ID: %d)", category.name, category.id);
        return CategoryResponse.from(category);
    }

    /**
     * Cập nhật danh mục.
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOrThrow(id);

        String newSlug = generateUniqueSlug(request.name(), slug -> {
            var existing = Category.findBySlug(slug);
            return existing.filter(c -> !c.id.equals(id));
        });

        category.name = request.name().trim();
        category.slug = newSlug;
        category.description = request.description();

        // Update parent
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BadRequestException("Danh mục không thể là parent của chính nó");
            }
            Category parent = findOrThrow(request.parentId());
            category.parent = parent;
        } else {
            category.parent = null;
        }

        LOG.infof("Category updated: %s (ID: %d)", category.name, category.id);
        return CategoryResponse.from(category);
    }

    /**
     * Soft delete danh mục.
     */
    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        category.isActive = false;
        LOG.infof("Category soft-deleted: %s (ID: %d)", category.name, category.id);
    }

    // ==================== PUBLIC ====================

    /**
     * Lấy cây danh mục (chỉ active, dạng tree từ root).
     */
    public List<CategoryResponse> getActiveTree() {
        List<Category> roots = Category.listRootCategories();
        return roots.stream()
                .map(CategoryResponse::withChildren)
                .toList();
    }

    /**
     * Lấy danh mục active theo slug.
     */
    public CategoryResponse getBySlug(String slug) {
        Category category = Category.findBySlug(slug)
                .filter(c -> c.isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", slug));
        return CategoryResponse.withChildren(category);
    }

    // ==================== HELPERS ====================

    private Category findOrThrow(Long id) {
        Category category = Category.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Danh mục", id);
        }
        return category;
    }

    /**
     * Generate URL-friendly slug từ tên (hỗ trợ tiếng Việt).
     */
    static String generateSlug(String input) {
        if (input == null || input.isBlank()) return "";
        // Normalize Unicode (decompose Vietnamese characters)
        String normalized = Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD);
        // Remove diacritical marks
        String noDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        // Replace đ/Đ
        noDiacritics = noDiacritics.replace("đ", "d");
        // Replace non-alphanumeric with hyphens
        String slug = noDiacritics.replaceAll("[^a-z0-9]+", "-");
        // Trim leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    /**
     * Generate unique slug, auto-append suffix if duplicate.
     */
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
