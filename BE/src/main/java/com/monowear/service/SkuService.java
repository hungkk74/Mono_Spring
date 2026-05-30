package com.monowear.service;

import com.monowear.dto.catalog.SkuRequest;
import com.monowear.dto.catalog.SkuResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Product;
import com.monowear.entity.Sku;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class SkuService {

    private static final Logger LOG = Logger.getLogger(SkuService.class);

    // ==================== ADMIN/STAFF ====================

    /**
     * Lấy danh sách SKU theo product (phân trang).
     */
    public PagedResponse<SkuResponse> listByProduct(Long productId, int page, int size) {
        var query = Sku.find("product.id", productId);
        long total = query.count();
        List<SkuResponse> items = query
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(e -> SkuResponse.from((Sku) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    /**
     * Lấy chi tiết SKU theo ID.
     */
    public SkuResponse getById(Long id) {
        Sku sku = findOrThrow(id);
        return SkuResponse.from(sku);
    }

    /**
     * Tạo SKU mới.
     */
    @Transactional
    public SkuResponse create(SkuRequest request) {
        // Validate product
        Product product = Product.findById(request.productId());
        if (product == null) {
            throw new ResourceNotFoundException("Sản phẩm", request.productId());
        }

        // Check duplicate sku_code
        if (Sku.findBySkuCode(request.skuCode()).isPresent()) {
            throw new DuplicateResourceException("Mã SKU [" + request.skuCode() + "] đã tồn tại");
        }

        Sku sku = new Sku();
        sku.product = product;
        sku.skuCode = request.skuCode().trim().toUpperCase();
        sku.size = request.size().trim();
        sku.color = request.color().trim();
        sku.price = request.price();
        sku.stock = request.stock();
        sku.isActive = true;
        sku.persist();

        LOG.infof("SKU created: %s (ID: %d, Product: %s)", sku.skuCode, sku.id, product.name);
        return SkuResponse.from(sku);
    }

    /**
     * Cập nhật SKU.
     */
    @Transactional
    public SkuResponse update(Long id, SkuRequest request) {
        Sku sku = findOrThrow(id);

        // Validate product
        Product product = Product.findById(request.productId());
        if (product == null) {
            throw new ResourceNotFoundException("Sản phẩm", request.productId());
        }

        // Check duplicate sku_code (exclude self)
        Sku.findBySkuCode(request.skuCode()).ifPresent(existing -> {
            if (!existing.id.equals(id)) {
                throw new DuplicateResourceException("Mã SKU [" + request.skuCode() + "] đã tồn tại");
            }
        });

        sku.product = product;
        sku.skuCode = request.skuCode().trim().toUpperCase();
        sku.size = request.size().trim();
        sku.color = request.color().trim();
        sku.price = request.price();
        sku.stock = request.stock();

        LOG.infof("SKU updated: %s (ID: %d)", sku.skuCode, sku.id);
        return SkuResponse.from(sku);
    }

    /**
     * Soft delete SKU.
     */
    @Transactional
    public void delete(Long id) {
        Sku sku = findOrThrow(id);
        sku.isActive = false;
        LOG.infof("SKU soft-deleted: %s (ID: %d)", sku.skuCode, sku.id);
    }

    // ==================== HELPERS ====================

    private Sku findOrThrow(Long id) {
        Sku sku = Sku.findById(id);
        if (sku == null) {
            throw new ResourceNotFoundException("SKU", id);
        }
        return sku;
    }
}
