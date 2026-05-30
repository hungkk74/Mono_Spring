package com.monowear.service;

import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.*;
import com.monowear.entity.*;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.InsufficientStockException;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Inject
    CouponService couponService;

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = User.findById(userId);
        if (user == null || !user.isActive) {
            throw new BadRequestException("Tai khoan khong hop le");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Sku sku = Sku.findById(itemReq.skuId());
            if (sku == null || !sku.isActive) {
                throw new ResourceNotFoundException("SKU", itemReq.skuId());
            }

            if (sku.stock < itemReq.quantity()) {
                throw new InsufficientStockException(sku.id, itemReq.quantity(), sku.stock);
            }

            int updated = Sku.deductStock(sku.id, itemReq.quantity(), sku.version);
            if (updated == 0) {
                Sku refreshed = Sku.findById(sku.id);
                int available = refreshed != null ? refreshed.stock : 0;
                throw new InsufficientStockException(sku.id, itemReq.quantity(), available);
            }

            BigDecimal effectivePrice = getEffectivePrice(sku);

            OrderItem item = new OrderItem();
            item.sku = sku;
            item.quantity = itemReq.quantity();
            item.unitPrice = effectivePrice;
            item.subtotal = effectivePrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            orderItems.add(item);

            subtotalAmount = subtotalAmount.add(item.subtotal);
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
        order.user = user;
        order.status = OrderStatus.PENDING;
        order.subtotalAmount = subtotalAmount;
        order.discountAmount = discountAmount;
        order.couponCode = couponCode;
        order.totalAmount = totalAmount;
        order.shippingAddress = request.shippingAddress().trim();
        order.paymentMethod = request.paymentMethod().trim();
        order.persist();

        for (OrderItem item : orderItems) {
            item.order = order;
            item.persist();
        }
        order.items = orderItems;

        LOG.infof("Order placed: #%d by User %d | Subtotal: %s | Discount: %s | Total: %s | Items: %d",
                order.id, userId, subtotalAmount, discountAmount, totalAmount, orderItems.size());

        return OrderResponse.withItems(order);
    }

    public PagedResponse<OrderResponse> listByUser(Long userId, int page, int size) {
        long total = Order.count("user.id", userId);

        // Bước 1: Lấy IDs phân trang (tránh Hibernate in-memory pagination khi JOIN FETCH)
        @SuppressWarnings("unchecked")
        List<Long> ids = Order.getEntityManager()
                .createQuery("SELECT o.id FROM Order o WHERE o.user.id = :uid ORDER BY o.createdAt DESC")
                .setParameter("uid", userId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        if (ids.isEmpty()) {
            return PagedResponse.of(List.of(), page, size, total);
        }

        // Bước 2: Fetch đầy đủ với JOIN FETCH (không pagination → không warning)
        List<OrderResponse> items = Order.find(
                "SELECT DISTINCT o FROM Order o " +
                "JOIN FETCH o.user " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.sku s " +
                "LEFT JOIN FETCH s.product " +
                "WHERE o.id IN ?1 ORDER BY o.createdAt DESC", ids)
                .list()
                .stream()
                .map(e -> OrderResponse.withItems((Order) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    public OrderResponse getByIdForUser(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Ban khong co quyen xem don hang nay");
        }
        return OrderResponse.withItems(order);
    }

    public OrderTrackingResponse getTrackingById(Long orderId) {
        Order order = Order.find(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items " +
                "WHERE o.id = ?1", orderId).firstResult();
        if (order == null) {
            throw new ResourceNotFoundException("Don hang", orderId);
        }
        return OrderTrackingResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Ban khong co quyen huy don hang nay");
        }
        if (order.status != OrderStatus.PENDING) {
            throw new BadRequestException("Chi co the huy don hang o trang thai PENDING");
        }

        restoreStock(order);
        order.status = OrderStatus.CANCELLED;
        LOG.infof("Order #%d cancelled by User %d", orderId, userId);
        return OrderResponse.withItems(order);
    }

    public PagedResponse<OrderResponse> listAll(int page, int size, OrderStatus status, String search) {
        var params = new java.util.HashMap<String, Object>();
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

        // Step 1: Count
        String countHql = "SELECT COUNT(o) FROM Order o " + whereClause;
        var countQuery = Order.getEntityManager().createQuery(countHql, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        if (total == 0) {
            return PagedResponse.of(List.of(), page, size, 0);
        }

        // Step 2: Fetch paginated IDs (avoids in-memory pagination with JOIN FETCH)
        String idHql = "SELECT o.id FROM Order o " + whereClause + " ORDER BY o.createdAt DESC";
        var idQuery = Order.getEntityManager().createQuery(idHql, Long.class);
        params.forEach(idQuery::setParameter);
        List<Long> ids = idQuery
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        if (ids.isEmpty()) {
            return PagedResponse.of(List.of(), page, size, total);
        }

        // Step 3: Fetch full entities by IDs with JOIN FETCH
        List<OrderResponse> items = Order.find(
                "SELECT DISTINCT o FROM Order o " +
                "JOIN FETCH o.user " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.sku s " +
                "LEFT JOIN FETCH s.product " +
                "WHERE o.id IN ?1 ORDER BY o.createdAt DESC", ids)
                .list()
                .stream()
                .map(e -> OrderResponse.withItems((Order) e))
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    public OrderResponse getById(Long orderId) {
        Order order = findOrThrow(orderId);
        return OrderResponse.withItems(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrThrow(orderId);

        OrderStatus currentStatus = order.status;
        OrderStatus newStatus = request.status();

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BadRequestException(
                    String.format("Khong the chuyen trang thai tu %s sang %s", currentStatus, newStatus),
                    "INVALID_STATUS_TRANSITION"
            );
        }

        if (newStatus == OrderStatus.CONFIRMED && currentStatus == OrderStatus.PENDING) {
            if ("MOMO".equalsIgnoreCase(order.paymentMethod)
                    || "BANK_TRANSFER".equalsIgnoreCase(order.paymentMethod)) {
                throw new BadRequestException("Khong the xac nhan thu cong don hang chua thanh toan qua cong thanh toan.", "PAYMENT_REQUIRED");
            }
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.status = newStatus;
        LOG.infof("Order #%d status: %s -> %s", orderId, currentStatus, newStatus);
        return OrderResponse.withItems(order);
    }

    private Order findOrThrow(Long id) {
        Order order = Order.find(
                "SELECT o FROM Order o " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.sku s " +
                "LEFT JOIN FETCH s.product " +
                "JOIN FETCH o.user " +
                "WHERE o.id = ?1", id).firstResult();
        if (order == null) {
            throw new ResourceNotFoundException("Don hang", id);
        }
        return order;
    }

    private BigDecimal getEffectivePrice(Sku sku) {
        if (sku.product != null && sku.product.isOnSale()) {
            return sku.price
                    .multiply(BigDecimal.valueOf(100L - sku.product.salePercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return sku.price;
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.items) {
            int updated = Sku.restoreStock(item.sku.id, item.quantity);
            if (updated > 0) {
                LOG.infof("Stock restored: SKU ID %d +%d", item.sku.id, item.quantity);
            }
        }
    }

    /**
     * Reorder: validate each item's SKU for availability, stock, and fetch current price.
     */
    public List<ReorderItemResponse> getReorderItems(Long orderId, Long userId) {
        Order order = findOrThrow(orderId);
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Ban khong co quyen thao tac don hang nay");
        }

        Set<OrderStatus> allowedStatuses = Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
        if (!allowedStatuses.contains(order.status)) {
            throw new BadRequestException("Chi co the mua lai don hang da giao hoac da huy");
        }

        // Batch load tất cả SKUs trong 1 query thay vì N+1
        Set<Long> skuIds = order.items.stream().map(i -> i.sku.id).collect(java.util.stream.Collectors.toSet());
        @SuppressWarnings("unchecked")
        List<Sku> skuList = Sku.getEntityManager()
                .createQuery("SELECT s FROM Sku s JOIN FETCH s.product WHERE s.id IN :ids")
                .setParameter("ids", skuIds)
                .getResultList();
        Map<Long, Sku> skuMap = skuList.stream().collect(java.util.stream.Collectors.toMap(s -> s.id, s -> s));

        return order.items.stream().map(item -> {
            Sku sku = skuMap.get(item.sku.id);
            if (sku == null || !sku.isActive || sku.product == null || !sku.product.isActive) {
                return new ReorderItemResponse(
                        item.sku.id, item.sku.skuCode,
                        item.sku.product != null ? item.sku.product.name : "San pham",
                        item.sku.product != null ? item.sku.product.slug : null,
                        item.sku.product != null ? item.sku.product.imageUrl : null,
                        item.sku.size, item.sku.color,
                        null, null, 0, false, "San pham da ngung kinh doanh"
                );
            }

            BigDecimal currentPrice = sku.price;
            BigDecimal salePrice = null;
            if (sku.product.isOnSale()) {
                salePrice = currentPrice
                        .multiply(BigDecimal.valueOf(100L - sku.product.salePercent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            boolean inStock = sku.stock > 0;
            String reason = inStock ? null : "Het hang";

            return new ReorderItemResponse(
                    sku.id, sku.skuCode,
                    sku.product.name, sku.product.slug, sku.product.imageUrl,
                    sku.size, sku.color,
                    currentPrice, salePrice, sku.stock,
                    inStock, reason
            );
        }).toList();
    }
}
