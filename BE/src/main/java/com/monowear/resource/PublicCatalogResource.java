package com.monowear.resource;

import com.monowear.dto.catalog.CategoryResponse;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.OrderTrackingResponse;
import com.monowear.dto.review.ReviewResponse;
import com.monowear.exception.BadRequestException;
import com.monowear.service.CategoryService;
import com.monowear.service.OrderService;
import com.monowear.service.ProductService;
import com.monowear.service.BannerService;
import com.monowear.service.ReviewService;
import com.monowear.service.ReviewService.ReviewStats;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

/**
 * Public Catalog APIs — Không yêu cầu đăng nhập.
 * Dành cho Frontend hiển thị danh mục, sản phẩm, chi tiết.
 */
@Path("/api/public")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicCatalogResource {

    @Inject
    CategoryService categoryService;

    @Inject
    ProductService productService;

    @Inject
    OrderService orderService;

    @Inject
    ReviewService reviewService;

    @Inject
    BannerService bannerService;

    // ==================== CATEGORIES ====================

    /**
     * GET /api/public/categories — Lấy cây danh mục active.
     */
    @GET
    @Path("/categories")
    public RestResponse<ApiResponse<List<CategoryResponse>>> getCategories() {
        return RestResponse.ok(ApiResponse.success(categoryService.getActiveTree()));
    }

    /**
     * GET /api/public/categories/{slug} — Chi tiết danh mục theo slug.
     */
    @GET
    @Path("/categories/{slug}")
    public RestResponse<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathParam("slug") String slug) {
        return RestResponse.ok(ApiResponse.success(categoryService.getBySlug(slug)));
    }

    // ==================== PRODUCTS ====================

    /**
     * GET /api/public/products — Danh sách sản phẩm (phân trang, lọc, tìm kiếm).
     */
    @GET
    @Path("/products")
    public RestResponse<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
             @QueryParam("page") @DefaultValue("0") int page,
             @QueryParam("size") @DefaultValue("20") int size,
             @QueryParam("categoryId") Long categoryId,
             @QueryParam("keyword") String keyword,
             @QueryParam("skuSizes") List<String> skuSizes,
             @QueryParam("skuColors") List<String> skuColors,
             @QueryParam("saleOnly") @DefaultValue("false") boolean saleOnly) {
        return RestResponse.ok(ApiResponse.success(productService.listActive(page, size, categoryId, keyword, skuSizes, skuColors, saleOnly)));
    }

    /**
     * GET /api/public/products/{slug} — Chi tiết sản phẩm theo slug (kèm SKU).
     */
    @GET
    @Path("/products/{slug}")
    public RestResponse<ApiResponse<ProductResponse>> getProductBySlug(@PathParam("slug") String slug) {
        return RestResponse.ok(ApiResponse.success(productService.getBySlug(slug)));
    }

    @GET
    @Path("/orders/{id}/tracking")
    public RestResponse<ApiResponse<OrderTrackingResponse>> trackOrder(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(orderService.getTrackingById(id)));
    }

    // ==================== REVIEWS ====================

    /**
     * GET /api/public/reviews — Lấy danh sách đánh giá của sản phẩm (công khai).
     */
    @GET
    @Path("/reviews")
    public RestResponse<ApiResponse<List<ReviewResponse>>> getProductReviews(@QueryParam("productId") Long productId) {
        if (productId == null) {
            throw new BadRequestException("Thiếu productId");
        }
        return RestResponse.ok(ApiResponse.success(reviewService.getByProduct(productId)));
    }

    /**
     * GET /api/public/reviews/stats — Thống kê đánh giá của sản phẩm (công khai).
     */
    @GET
    @Path("/reviews/stats")
    public RestResponse<ApiResponse<ReviewStats>> getProductReviewStats(@QueryParam("productId") Long productId) {
        if (productId == null) {
            throw new BadRequestException("Thiếu productId");
        }
        return RestResponse.ok(ApiResponse.success(reviewService.getStats(productId)));
    }

    // ==================== BANNERS ====================

    /**
     * GET /api/public/banners — Lấy danh sách banner active (sắp theo displayOrder).
     */
    @GET
    @Path("/banners")
    public RestResponse<ApiResponse<java.util.List<com.monowear.dto.banner.BannerResponse>>> getBanners() {
        return RestResponse.ok(ApiResponse.success(bannerService.listActive()));
    }
}
