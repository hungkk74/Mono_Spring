package com.monowear.service;

import com.monowear.dto.catalog.SkuRequest;
import com.monowear.dto.catalog.SkuResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Product;
import com.monowear.entity.Sku;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.ProductRepository;
import com.monowear.repository.SkuRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkuService {

    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final EntityManager em;

    public PagedResponse<SkuResponse> listByProduct(Long productId, int page, int size) {
        List<Long> ids = em.createQuery("SELECT s.id FROM Sku s WHERE s.product.id = :pid", Long.class)
                .setParameter("pid", productId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
        long total = skuRepository.countByProductId(productId);
        List<SkuResponse> items = ids.isEmpty() ? List.of() :
                skuRepository.findAllById(ids).stream().map(SkuResponse::from).toList();
        return PagedResponse.of(items, page, size, total);
    }

    public SkuResponse getById(Long id) {
        Sku sku = findOrThrow(id);
        return SkuResponse.from(sku);
    }

    @Transactional
    public SkuResponse create(SkuRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.productId()));

        if (skuRepository.findBySkuCode(request.skuCode()).isPresent()) {
            throw new DuplicateResourceException("Mã SKU [" + request.skuCode() + "] đã tồn tại");
        }

        Sku sku = new Sku();
        sku.setProduct(product);
        sku.setSkuCode(request.skuCode().trim().toUpperCase());
        sku.setSize(request.size().trim());
        sku.setColor(request.color().trim());
        sku.setPrice(request.price());
        sku.setStock(request.stock());
        sku.setIsActive(true);
        skuRepository.save(sku);

        log.info("SKU created: {} (ID: {}, Product: {})", sku.getSkuCode(), sku.getId(), product.getName());
        return SkuResponse.from(sku);
    }

    @Transactional
    public SkuResponse update(Long id, SkuRequest request) {
        Sku sku = findOrThrow(id);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.productId()));

        skuRepository.findBySkuCode(request.skuCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Mã SKU [" + request.skuCode() + "] đã tồn tại");
            }
        });

        sku.setProduct(product);
        sku.setSkuCode(request.skuCode().trim().toUpperCase());
        sku.setSize(request.size().trim());
        sku.setColor(request.color().trim());
        sku.setPrice(request.price());
        sku.setStock(request.stock());

        log.info("SKU updated: {} (ID: {})", sku.getSkuCode(), sku.getId());
        return SkuResponse.from(sku);
    }

    @Transactional
    public void delete(Long id) {
        Sku sku = findOrThrow(id);
        sku.setIsActive(false);
        log.info("SKU soft-deleted: {} (ID: {})", sku.getSkuCode(), sku.getId());
    }

    private Sku findOrThrow(Long id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU", id));
    }
}
