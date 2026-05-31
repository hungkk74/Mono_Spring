package com.monowear.service;

import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.*;
import com.monowear.entity.*;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.InsufficientStockException;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.OrderRepository;
import com.monowear.repository.SkuRepository;
import com.monowear.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final OrderRepository orderRepository;
    private final SkuRepository skuRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final EntityManager em;

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.getIsActive()) {
            throw new BadRequestException("Tai khoan khong hop le");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Sku sku = skuRepository.findById(itemReq.skuId()).orElse(null);
            if (sku == null || !sku.getIsActive()) {
                throw new ResourceNotFoundException("SKU", itemReq.skuId());
            }

            if (sku.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(sku.getId(), itemReq.quantity(), sku.getStock());
            }

            int updated = skuRepository.deductStock(sku.getId(), itemReq.quantity());
            if (updated == 0) {
                Sku refreshed = skuRepository.findById(sku.getId()).orElse(null);
                int available = refreshed != null ? refreshed.getStock() : 0;
                throw new InsufficientStockException(sku.getId(), itemReq.quantity(), available);
            }

            BigDecimal effectivePrice = getEffectivePrice(sku);

            OrderItem item = new OrderItem();
            item.setSku(sku);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(effectivePrice);
            item.setSubtotal(effectivePrice.multiply(BigDecimal.valueOf(itemReq.quantity())));
            orderItems.add(item);

            subtotalAmount = subtotalAmount.add(item.getSubtotal());
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        String couponCode = null;
        BigDecimal totalAmount = subtotalAmount;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            var coupon = couponService.applyAndConsume(request.couponCode(), subtotalAmount);
            discountAmount = coupon.discountAmount();
            couponCode = coupon.code();
            totalAmount = coupon.totalAmount();
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotalAmount(subtotalAmount);
        order.setDiscountAmount(discountAmount);
        order.setCouponCode(couponCode);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod().trim());
        orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        orderRepository.save(order);

        log.info("Order placed: #{} by User {} | Subtotal: {} | Discount: {} | Total: {} | Items: {}",
                order.getId(), userId, subtotalAmount, discountAmount, totalAmount, orderItems.size());

        return OrderResponse.withItems(order);
    }

    public PagedResponse<OrderResponse> listByUser(Long userId, int page, int size) {
        long total = orderRepository.countByUserId(userId);

        List<Long> ids = em.createQuery("SELECT o.id FROM Order o WHERE o.user.id = :uid ORDER BY o.createdAt DESC", Long.class)
                .setParameter("uid", userId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        if (ids.isEmpty()) return PagedResponse.of(List.of(), page, size, total);

        List<OrderResponse> items = orderRepository.findAllWithDetailsByIds(ids)
                .stream().map(OrderResponse::withItems).toList();
        return PagedResponse.of(items, page, size, total);
    }

    public OrderResponse getByIdForUser(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Ban khong co quyen xem don hang nay");
        }
        return OrderResponse.withItems(order);
    }

    public OrderTrackingResponse getTrackingById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Don hang", orderId));
        return OrderTrackingResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Ban khong co quyen huy don hang nay");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chi co the huy don hang o trang thai PENDING");
        }

        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order #{} cancelled by User {}", orderId, userId);
        return OrderResponse.withItems(order);
    }

    public PagedResponse<OrderResponse> listAll(int page, int size, OrderStatus status, String search) {
        var params = new HashMap<String, Object>();
        String whereClause = "WHERE 1=1";

        if (status != null) {
            whereClause += " AND o.status = :status";
            params.put("status", status);
        }
        if (search != null && !search.isBlank()) {
            String trimmed = search.trim();
            try {
                Long searchId = Long.parseLong(trimmed);
                whereClause += " AND o.id = :searchId";
                params.put("searchId", searchId);
            } catch (NumberFormatException e) {
                whereClause += " AND LOWER(o.user.fullName) LIKE :searchName";
                params.put("searchName", "%" + trimmed.toLowerCase() + "%");
            }
        }

        String countHql = "SELECT COUNT(o) FROM Order o " + whereClause;
        var countQuery = em.createQuery(countHql, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        if (total == 0) return PagedResponse.of(List.of(), page, size, 0);

        String idHql = "SELECT o.id FROM Order o " + whereClause + " ORDER BY o.createdAt DESC";
        var idQuery = em.createQuery(idHql, Long.class);
        params.forEach(idQuery::setParameter);
        List<Long> ids = idQuery.setFirstResult(page * size).setMaxResults(size).getResultList();

        if (ids.isEmpty()) return PagedResponse.of(List.of(), page, size, total);

        List<OrderResponse> items = orderRepository.findAllWithDetailsByIds(ids)
                .stream().map(OrderResponse::withItems).toList();
        return PagedResponse.of(items, page, size, total);
    }

    public OrderResponse getById(Long orderId) {
        Order order = findOrThrow(orderId);
        return OrderResponse.withItems(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrThrow(orderId);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.status();

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BadRequestException(
                    String.format("Khong the chuyen trang thai tu %s sang %s", currentStatus, newStatus),
                    "INVALID_STATUS_TRANSITION"
            );
        }

        if (newStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            if ("MOMO".equalsIgnoreCase(order.getPaymentMethod())
                    || "BANK_TRANSFER".equalsIgnoreCase(order.getPaymentMethod())) {
                throw new BadRequestException("Khong the xac nhan thu cong don hang chua thanh toan qua cong thanh toan.", "PAYMENT_REQUIRED");
            }
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        log.info("Order #{} status: {} -> {}", orderId, currentStatus, newStatus);
        return OrderResponse.withItems(order);
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Don hang", id));
    }

    private BigDecimal getEffectivePrice(Sku sku) {
        if (sku.getProduct() != null && sku.getProduct().isOnSale()) {
            return sku.getPrice()
                    .multiply(BigDecimal.valueOf(100L - sku.getProduct().getSalePercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return sku.getPrice();
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            int updated = skuRepository.restoreStock(item.getSku().getId(), item.getQuantity());
            if (updated > 0) {
                log.info("Stock restored: SKU ID {} +{}", item.getSku().getId(), item.getQuantity());
            }
        }
    }

    public List<ReorderItemResponse> getReorderItems(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Ban khong co quyen thao tac don hang nay");
        }

        Set<OrderStatus> allowedStatuses = Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
        if (!allowedStatuses.contains(order.getStatus())) {
            throw new BadRequestException("Chi co the mua lai don hang da giao hoac da huy");
        }

        Set<Long> skuIds = order.getItems().stream().map(i -> i.getSku().getId()).collect(Collectors.toSet());
        @SuppressWarnings("unchecked")
        List<Sku> skuList = em.createQuery("SELECT s FROM Sku s JOIN FETCH s.product WHERE s.id IN :ids")
                .setParameter("ids", skuIds)
                .getResultList();
        Map<Long, Sku> skuMap = skuList.stream().collect(Collectors.toMap(Sku::getId, s -> s));

        return order.getItems().stream().map(item -> {
            Sku sku = skuMap.get(item.getSku().getId());
            if (sku == null || !sku.getIsActive() || sku.getProduct() == null || !sku.getProduct().getIsActive()) {
                return new ReorderItemResponse(
                        item.getSku().getId(), item.getSku().getSkuCode(),
                        item.getSku().getProduct() != null ? item.getSku().getProduct().getName() : "San pham",
                        item.getSku().getProduct() != null ? item.getSku().getProduct().getSlug() : null,
                        item.getSku().getProduct() != null ? item.getSku().getProduct().getImageUrl() : null,
                        item.getSku().getSize(), item.getSku().getColor(),
                        null, null, 0, false, "San pham da ngung kinh doanh"
                );
            }

            BigDecimal currentPrice = sku.getPrice();
            BigDecimal salePrice = null;
            if (sku.getProduct().isOnSale()) {
                salePrice = currentPrice
                        .multiply(BigDecimal.valueOf(100L - sku.getProduct().getSalePercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            boolean inStock = sku.getStock() > 0;
            String reason = inStock ? null : "Het hang";

            return new ReorderItemResponse(
                    sku.getId(), sku.getSkuCode(),
                    sku.getProduct().getName(), sku.getProduct().getSlug(), sku.getProduct().getImageUrl(),
                    sku.getSize(), sku.getColor(),
                    currentPrice, salePrice, sku.getStock(),
                    inStock, reason
            );
        }).toList();
    }
}
