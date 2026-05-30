package com.monowear.service;

import com.monowear.dto.catalog.ProductRequest;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Category;
import com.monowear.entity.Product;
import com.monowear.entity.Sku;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ProductService {

    private static final Logger LOG = Logger.getLogger(ProductService.class);

    @Inject
    CategoryService categoryService;

    // ==================== ADMIN/STAFF ====================

    /**
     * Lấy danh sách sản phẩm (phân trang, bao gồm inactive).
     */
    public PagedResponse<ProductResponse> listAll(int page, int size, Long categoryId, String keyword) {
        String countQuery = "SELECT COUNT(p) FROM Product p WHERE 1=1";
        String idQuery = "SELECT p.id FROM Product p WHERE 1=1";
        var params = new java.util.HashMap<String, Object>();

        if (categoryId != null) {
            countQuery += " AND p.category.id = :categoryId";
            idQuery += " AND p.category.id = :categoryId";
            params.put("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            countQuery += " AND (LOWER(p.name) LIKE :keyword OR LOWER(p.description) LIKE :keyword OR LOWER(p.material) LIKE :keyword OR LOWER(p.category.name) LIKE :keyword)";
            idQuery += " AND (LOWER(p.name) LIKE :keyword OR LOWER(p.description) LIKE :keyword OR LOWER(p.material) LIKE :keyword OR LOWER(p.category.name) LIKE :keyword)";
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        idQuery += " ORDER BY p.id DESC";

        // Step 1: Count total
        var countJpql = Product.getEntityManager().createQuery(countQuery);
        for (var entry : params.entrySet()) {
            countJpql.setParameter(entry.getKey(), entry.getValue());
        }
        long total = (long) countJpql.getSingleResult();

        if (total == 0) {
            return PagedResponse.of(List.of(), page, size, 0);
        }

        // Step 2: Get paginated IDs
        var idJpql = Product.getEntityManager().createQuery(idQuery);
        for (var entry : params.entrySet()) {
            idJpql.setParameter(entry.getKey(), entry.getValue());
        }
        @SuppressWarnings("unchecked")
        List<Long> ids = idJpql
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // Step 3: Fetch detail with Category and SKUs in 1 JOIN FETCH query
        List<ProductResponse> items = Product.find(
                "SELECT DISTINCT p FROM Product p " +
                "JOIN FETCH p.category " +
                "LEFT JOIN FETCH p.skus " +
                "WHERE p.id IN ?1 ORDER BY p.id DESC", ids)
                .list()
                .stream()
                .map(e -> ProductResponse.withSkus((Product) e))
                .toList();

        return PagedResponse.of(items, page, size, total);
    }

    /**
     * Lấy chi tiết sản phẩm (kèm SKU) theo ID.
     */
    public ProductResponse getById(Long id) {
        Product product = findOrThrow(id);
        return ProductResponse.withSkus(product);
    }

    /**
     * Tạo sản phẩm mới.
     */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        // Validate category exists
        Category category = Category.findById(request.categoryId());
        if (category == null) {
            throw new ResourceNotFoundException("Danh mục", request.categoryId());
        }

        String slug = generateUniqueSlug(request.name(), Product::findBySlug);
        Product product = new Product();
        product.name = request.name().trim();
        product.slug = slug;
        product.material = request.material();
        product.description = request.description();
        product.imageUrl = request.imageUrl();
        product.category = category;
        product.isActive = true;
        product.persist();

        LOG.infof("Product created: %s (ID: %d)", product.name, product.id);
        return ProductResponse.from(product);
    }

    /**
     * Cập nhật sản phẩm.
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);

        // Validate category
        Category category = Category.findById(request.categoryId());
        if (category == null) {
            throw new ResourceNotFoundException("Danh mục", request.categoryId());
        }

        String newSlug = generateUniqueSlug(request.name(), slug -> {
            var existing = Product.findBySlug(slug);
            return existing.filter(p -> !p.id.equals(id));
        });

        product.name = request.name().trim();
        product.slug = newSlug;
        product.material = request.material();
        product.description = request.description();
        product.imageUrl = request.imageUrl();
        product.category = category;

        LOG.infof("Product updated: %s (ID: %d)", product.name, product.id);
        return ProductResponse.from(product);
    }

    /**
     * Soft delete sản phẩm + tất cả SKU liên quan.
     */
    @Transactional
    public void delete(Long id) {
        Product product = findOrThrow(id);
        product.isActive = false;
        // Deactivate all related SKUs
        if (product.skus != null) {
            for (Sku sku : product.skus) {
                sku.isActive = false;
            }
        }
        LOG.infof("Product soft-deleted: %s (ID: %d)", product.name, product.id);
    }

    // ==================== PUBLIC ====================

    /**
     * Lấy danh sách sản phẩm active (phân trang, lọc theo category, tìm kiếm, size, color).
     * Khi lọc theo category cha, bao gồm cả sản phẩm thuộc danh mục con.
     */
    public PagedResponse<ProductResponse> listActive(int page, int size, Long categoryId, String keyword, java.util.List<String> skuSizes, java.util.List<String> skuColors) {
        return listActive(page, size, categoryId, keyword, skuSizes, skuColors, false);
    }

    public PagedResponse<ProductResponse> listActive(int page, int size, Long categoryId, String keyword, java.util.List<String> skuSizes, java.util.List<String> skuColors, boolean saleOnly) {
        StringBuilder query = new StringBuilder("SELECT DISTINCT p FROM Product p JOIN FETCH p.category");
        StringBuilder countQuery = new StringBuilder("SELECT count(DISTINCT p) FROM Product p");
        
        boolean hasSkuJoin = (skuSizes != null && !skuSizes.isEmpty()) || (skuColors != null && !skuColors.isEmpty());
        if (hasSkuJoin) {
            query.append(" LEFT JOIN p.skus s");
            countQuery.append(" LEFT JOIN p.skus s");
        }
        
        query.append(" WHERE p.isActive = true");
        countQuery.append(" WHERE p.isActive = true");
        
        var params = new java.util.HashMap<String, Object>();

        if (categoryId != null) {
            // Collect this category + all child category IDs
            java.util.List<Long> categoryIds = collectCategoryIds(categoryId);
            query.append(" AND p.category.id IN :categoryIds");
            countQuery.append(" AND p.category.id IN :categoryIds");
            params.put("categoryIds", categoryIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.append(" AND (LOWER(p.name) LIKE :keyword OR LOWER(p.description) LIKE :keyword OR LOWER(p.material) LIKE :keyword OR LOWER(p.category.name) LIKE :keyword)");
            countQuery.append(" AND (LOWER(p.name) LIKE :keyword OR LOWER(p.description) LIKE :keyword OR LOWER(p.material) LIKE :keyword OR LOWER(p.category.name) LIKE :keyword)");
            params.put("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (saleOnly) {
            query.append(" AND p.salePercent IS NOT NULL AND p.salePercent > 0")
                    .append(" AND (p.saleStartAt IS NULL OR p.saleStartAt <= CURRENT_TIMESTAMP)")
                    .append(" AND (p.saleEndAt IS NULL OR p.saleEndAt >= CURRENT_TIMESTAMP)");
            countQuery.append(" AND p.salePercent IS NOT NULL AND p.salePercent > 0")
                    .append(" AND (p.saleStartAt IS NULL OR p.saleStartAt <= CURRENT_TIMESTAMP)")
                    .append(" AND (p.saleEndAt IS NULL OR p.saleEndAt >= CURRENT_TIMESTAMP)");
        }
        if (skuSizes != null && !skuSizes.isEmpty()) {
            query.append(" AND s.size IN :skuSizes AND s.isActive = true");
            countQuery.append(" AND s.size IN :skuSizes AND s.isActive = true");
            params.put("skuSizes", skuSizes);
        }
        if (skuColors != null && !skuColors.isEmpty()) {
            query.append(" AND s.color IN :skuColors AND s.isActive = true");
            countQuery.append(" AND s.color IN :skuColors AND s.isActive = true");
            params.put("skuColors", skuColors);
        }

        jakarta.persistence.Query qCount = Product.getEntityManager().createQuery(countQuery.toString());
        for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
            qCount.setParameter(entry.getKey(), entry.getValue());
        }
        long total = (long) qCount.getSingleResult();

        var panacheQuery = Product.find(query.toString(), params);
        List<ProductResponse> items = panacheQuery
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(e -> ProductResponse.from((Product) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    /**
     * Lấy chi tiết sản phẩm active theo slug (cho frontend).
     */
    public ProductResponse getBySlug(String slug) {
        Product product = Product.findBySlug(slug)
                .filter(p -> p.isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", slug));
        return ProductResponse.withSkus(product);
    }

    // ==================== HELPERS ====================

    private Product findOrThrow(Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Sản phẩm", id);
        }
        return product;
    }

    /**
     * Thu thập categoryId + tất cả ID danh mục con bằng 1 query duy nhất.
     * Trước: đệ quy N+1 queries. Sau: 1 SELECT lấy tất cả children.
     */
    private java.util.List<Long> collectCategoryIds(Long rootId) {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        ids.add(rootId);
        // Lấy tất cả active children (1 query flat, không đệ quy)
        @SuppressWarnings("unchecked")
        java.util.List<Long> childIds = Category.getEntityManager()
                .createQuery("SELECT c.id FROM Category c WHERE c.parent.id = :pid AND c.isActive = true")
                .setParameter("pid", rootId)
                .getResultList();
        for (Long childId : childIds) {
            ids.addAll(collectCategoryIds(childId)); // chỉ đệ quy nếu có grandchildren
        }
        return ids;
    }

    /**
     * Generate unique slug, auto-append suffix if duplicate.
     */
    private String generateUniqueSlug(String name, java.util.function.Function<String, java.util.Optional<Product>> finder) {
        String baseSlug = CategoryService.generateSlug(name);
        String slug = baseSlug;
        int counter = 1;
        while (finder.apply(slug).isPresent()) {
            slug = baseSlug + "-" + (++counter);
        }
        return slug;
    }
}
