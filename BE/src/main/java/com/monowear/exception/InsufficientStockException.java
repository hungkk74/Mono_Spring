package com.monowear.exception;


public class InsufficientStockException extends RuntimeException {

    private final Long skuId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long skuId, int requested, int available) {
        super(String.format("SKU [%d] không đủ tồn kho: yêu cầu %d, còn lại %d", skuId, requested, available));
        this.skuId = skuId;
        this.requested = requested;
        this.available = available;
    }

    public Long getSkuId() {
        return skuId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
