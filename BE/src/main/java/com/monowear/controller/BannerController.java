package com.monowear.controller;

import com.monowear.dto.banner.BannerRequest;
import com.monowear.dto.banner.BannerResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BannerResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.listAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerResponse>> create(@Valid @RequestBody BannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(bannerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> update(@PathVariable Long id, @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(@RequestBody List<Long> ids) {
        bannerService.reorder(ids);
        return ResponseEntity.ok(ApiResponse.success("Sắp xếp lại thành công", null));
    }
}
