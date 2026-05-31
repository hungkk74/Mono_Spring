package com.monowear.service;

import com.monowear.dto.banner.BannerRequest;
import com.monowear.dto.banner.BannerResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.Banner;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;

    public PagedResponse<BannerResponse> listAll(int page, int size) {
        long total = bannerRepository.countByIsActiveTrue();
        List<BannerResponse> items = bannerRepository
                .findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .map(BannerResponse::from)
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    public BannerResponse getById(Long id) {
        return BannerResponse.from(findOrThrow(id));
    }

    @Transactional
    public BannerResponse create(BannerRequest request) {
        Banner banner = new Banner();
        banner.setTitle(request.title().trim());
        banner.setSubtitle(request.subtitle());
        banner.setMediaUrl(request.mediaUrl().trim());
        banner.setMediaType(request.mediaType());
        banner.setThumbnailUrl(request.thumbnailUrl());
        banner.setLinkUrl(request.linkUrl());
        banner.setCtaText(request.ctaText());
        banner.setDisplayOrder(request.displayOrder() != null
                ? request.displayOrder()
                : bannerRepository.findMaxDisplayOrder() + 1);
        banner.setIsActive(true);

        bannerRepository.save(banner);
        log.info("Banner created: {} (ID: {}, type: {})", banner.getTitle(), banner.getId(), banner.getMediaType());
        return BannerResponse.from(banner);
    }

    @Transactional
    public BannerResponse update(Long id, BannerRequest request) {
        Banner banner = findOrThrow(id);
        banner.setTitle(request.title().trim());
        banner.setSubtitle(request.subtitle());
        banner.setMediaUrl(request.mediaUrl().trim());
        banner.setMediaType(request.mediaType());
        banner.setThumbnailUrl(request.thumbnailUrl());
        banner.setLinkUrl(request.linkUrl());
        banner.setCtaText(request.ctaText());
        if (request.displayOrder() != null) {
            banner.setDisplayOrder(request.displayOrder());
        }

        log.info("Banner updated: {} (ID: {})", banner.getTitle(), banner.getId());
        return BannerResponse.from(banner);
    }

    @Transactional
    public void delete(Long id) {
        Banner banner = findOrThrow(id);
        banner.setIsActive(false);
        log.info("Banner soft-deleted: {} (ID: {})", banner.getTitle(), banner.getId());
    }

    @Transactional
    public void reorder(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Banner banner = findOrThrow(ids.get(i));
            banner.setDisplayOrder(i + 1);
        }
        log.info("Banner reordered: {} items", ids.size());
    }

    public List<BannerResponse> listActive() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(BannerResponse::from).toList();
    }

    private Banner findOrThrow(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", id));
    }
}
