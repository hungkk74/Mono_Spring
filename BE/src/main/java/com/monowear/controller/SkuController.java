package com.monowear.controller;

import com.monowear.dto.catalog.SkuRequest;
import com.monowear.dto.catalog.SkuResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.SkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/skus")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SkuResponse>>> listByProduct(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(skuService.listByProduct(productId, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkuResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skuService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SkuResponse>> create(@Valid @RequestBody SkuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(skuService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkuResponse>> update(@PathVariable Long id, @Valid @RequestBody SkuRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        skuService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
