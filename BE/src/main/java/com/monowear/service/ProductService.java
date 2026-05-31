package com.monowear.service;

import com.monowear.dto.catalog.ProductRequest;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Category;
import com.monowear.entity.Product;
import com.monowear.entity.Sku;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.CategoryRepository;
import com.monowear.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager em;

    // ==================== ADMIN/STAFF ====================

    public PagedResponse<ProductResponse> listAll(int page, int size, Long categoryId, String keyword) {
        String countQuery = "SELECT COUNT(p) FROM Product p WHERE 1=1";
        String idQuery = "SELECT p.id FROM Product p WHERE 1=1";
        var params = new HashMap<String, Object>();

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

        var countJpql = em.createQuery(countQuery, Long.class);
        params.forEach(countJpql::setParameter);
        long total = countJpql.getSingleResult();

        if (total == 0) return PagedResponse.of(List.of(), page, size, 0);

        var idJpql = em.createQuery(idQuery, Long.class);
        params.forEach(idJpql::setParameter);
        List<Long> ids = idJpql.setFirstResult(page * size).setMaxResults(size).getResultList();

        List<ProductResponse> items = productRepository.findAllWithDetailsByIds(ids)
                .stream()
                .map(ProductResponse::withSkus)
                .toList();

        return PagedResponse.of(items, page, size, total);
    }

    public ProductResponse getById(Long id) {
        Product product = findOrThrow(id);
        return ProductResponse.withSkus(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.categoryId()));

        String slug = generateUniqueSlug(request.name(), productRepository::findBySlug);
        Product product = new Product();
        product.setName(request.name().trim());
        product.setSlug(slug);
        product.setMaterial(request.material());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);
        product.setIsActive(true);
        productRepository.save(product);

        log.info("Product created: {} (ID: {})", product.getName(), product.getId());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.categoryId()));

        String newSlug = generateUniqueSlug(request.name(), slug -> {
            var existing = productRepository.findBySlug(slug);
            return existing.filter(p -> !p.getId().equals(id));
        });

        product.setName(request.name().trim());
        product.setSlug(newSlug);
        product.setMaterial(request.material());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);

        log.info("Product updated: {} (ID: {})", product.getName(), product.getId());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findOrThrow(id);
        product.setIsActive(false);
        if (product.getSkus() != null) {
            for (Sku sku : product.getSkus()) {
                sku.setIsActive(false);
            }
        }
        log.info("Product soft-deleted: {} (ID: {})", product.getName(), product.getId());
    }

    // ==================== PUBLIC ====================

    public PagedResponse<ProductResponse> listActive(int page, int size, Long categoryId, String keyword, List<String> skuSizes, List<String> skuColors) {
        return listActive(page, size, categoryId, keyword, skuSizes, skuColors, false);
    }

    public PagedResponse<ProductResponse> listActive(int page, int size, Long categoryId, String keyword, List<String> skuSizes, List<String> skuColors, boolean saleOnly) {
        StringBuilder query = new StringBuilder("SELECT DISTINCT p FROM Product p JOIN FETCH p.category");
        StringBuilder countQuery = new StringBuilder("SELECT count(DISTINCT p) FROM Product p");

        boolean hasSkuJoin = (skuSizes != null && !skuSizes.isEmpty()) || (skuColors != null && !skuColors.isEmpty());
        if (hasSkuJoin) {
            query.append(" LEFT JOIN p.skus s");
            countQuery.append(" LEFT JOIN p.skus s");
        }

        query.append(" WHERE p.isActive = true");
        countQuery.append(" WHERE p.isActive = true");

        var params = new HashMap<String, Object>();

        if (categoryId != null) {
            List<Long> categoryIds = collectCategoryIds(categoryId);
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

        var qCount = em.createQuery(countQuery.toString(), Long.class);
        params.forEach(qCount::setParameter);
        long total = qCount.getSingleResult();

        TypedQuery<Product> dataQuery = em.createQuery(query.toString(), Product.class);
        params.forEach(dataQuery::setParameter);
        List<ProductResponse> items = dataQuery
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", slug));
        return ProductResponse.withSkus(product);
    }

    // ==================== HELPERS ====================

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
    }

    private List<Long> collectCategoryIds(Long rootId) {
        List<Long> ids = new ArrayList<>();
        ids.add(rootId);
        List<Long> childIds = categoryRepository.findChildIds(rootId);
        for (Long childId : childIds) {
            ids.addAll(collectCategoryIds(childId));
        }
        return ids;
    }

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
