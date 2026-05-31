package com.monowear.controller;

import com.monowear.dto.banner.BannerResponse;
import com.monowear.dto.catalog.CategoryResponse;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.OrderTrackingResponse;
import com.monowear.dto.review.ReviewResponse;
import com.monowear.exception.BadRequestException;
import com.monowear.service.*;
import com.monowear.service.ReviewService.ReviewStats;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final BannerService bannerService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getActiveTree()));
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getBySlug(slug)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> skuSizes,
            @RequestParam(required = false) List<String> skuColors,
            @RequestParam(defaultValue = "false") boolean saleOnly) {
        return ResponseEntity.ok(ApiResponse.success(productService.listActive(page, size, categoryId, keyword, skuSizes, skuColors, saleOnly)));
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getBySlug(slug)));
    }

    @GetMapping("/orders/{id}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getTrackingById(id)));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(@RequestParam Long productId) {
        if (productId == null) throw new BadRequestException("Thiếu productId");
        return ResponseEntity.ok(ApiResponse.success(reviewService.getByProduct(productId)));
    }

    @GetMapping("/reviews/stats")
    public ResponseEntity<ApiResponse<ReviewStats>> getProductReviewStats(@RequestParam Long productId) {
        if (productId == null) throw new BadRequestException("Thiếu productId");
        return ResponseEntity.ok(ApiResponse.success(reviewService.getStats(productId)));
    }

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners() {
        return ResponseEntity.ok(ApiResponse.success(bannerService.listActive()));
    }
}
