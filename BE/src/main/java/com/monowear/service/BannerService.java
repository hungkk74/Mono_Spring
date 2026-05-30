package com.monowear.service;

import com.monowear.dto.banner.BannerRequest;
import com.monowear.dto.banner.BannerResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Banner;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class BannerService {

    private static final Logger LOG = Logger.getLogger(BannerService.class);

    // ==================== ADMIN ====================

    /**
     * Lấy tất cả banner active, phân trang.
     */
    public PagedResponse<BannerResponse> listAll(int page, int size) {
        var query = Banner.find("isActive = true ORDER BY displayOrder ASC");
        long total = Banner.count("isActive", true);
        List<BannerResponse> items = query
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(e -> BannerResponse.from((Banner) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    /**
     * Lấy chi tiết banner theo ID.
     */
    public BannerResponse getById(Long id) {
        return BannerResponse.from(findOrThrow(id));
    }

    /**
     * Tạo banner mới.
     */
    @Transactional
    public BannerResponse create(BannerRequest request) {
        Banner banner = new Banner();
        banner.title = request.title().trim();
        banner.subtitle = request.subtitle();
        banner.mediaUrl = request.mediaUrl().trim();
        banner.mediaType = request.mediaType();
        banner.thumbnailUrl = request.thumbnailUrl();
        banner.linkUrl = request.linkUrl();
        banner.ctaText = request.ctaText();
        banner.displayOrder = request.displayOrder() != null
                ? request.displayOrder()
                : Banner.maxDisplayOrder() + 1;
        banner.isActive = true;

        banner.persist();
        LOG.infof("Banner created: %s (ID: %d, type: %s)", banner.title, banner.id, banner.mediaType);
        return BannerResponse.from(banner);
    }

    /**
     * Cập nhật banner.
     */
    @Transactional
    public BannerResponse update(Long id, BannerRequest request) {
        Banner banner = findOrThrow(id);
        banner.title = request.title().trim();
        banner.subtitle = request.subtitle();
        banner.mediaUrl = request.mediaUrl().trim();
        banner.mediaType = request.mediaType();
        banner.thumbnailUrl = request.thumbnailUrl();
        banner.linkUrl = request.linkUrl();
        banner.ctaText = request.ctaText();
        if (request.displayOrder() != null) {
            banner.displayOrder = request.displayOrder();
        }

        LOG.infof("Banner updated: %s (ID: %d)", banner.title, banner.id);
        return BannerResponse.from(banner);
    }

    /**
     * Soft delete banner.
     */
    @Transactional
    public void delete(Long id) {
        Banner banner = findOrThrow(id);
        banner.isActive = false;
        LOG.infof("Banner soft-deleted: %s (ID: %d)", banner.title, banner.id);
    }

    /**
     * Sắp xếp lại thứ tự hiển thị banner.
     */
    @Transactional
    public void reorder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Banner banner = findOrThrow(ids.get(i));
            banner.displayOrder = i + 1;
        }
        LOG.infof("Banner reordered: %d items", ids.size());
    }

    // ==================== PUBLIC ====================

    /**
     * Lấy danh sách banner active (sắp theo displayOrder).
     */
    public List<BannerResponse> listActive() {
        return Banner.listActiveOrdered()
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    // ==================== HELPERS ====================

    private Banner findOrThrow(Long id) {
        Banner banner = Banner.findById(id);
        if (banner == null) {
            throw new ResourceNotFoundException("Banner", id);
        }
        return banner;
    }
}
