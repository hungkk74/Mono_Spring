package com.monowear.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress,

        @NotBlank(message = "Phương thức thanh toán không được để trống")
        @Size(max = 50, message = "Phương thức thanh toán tối đa 50 ký tự")
        String paymentMethod,

        @Size(max = 50, message = "Ma giam gia toi da 50 ky tu")
        String couponCode,

        @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
        @Valid
        List<OrderItemRequest> items
) {
}
