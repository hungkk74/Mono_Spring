package com.monowear.dto.order;

import com.monowear.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        OrderStatus status
) {
}
