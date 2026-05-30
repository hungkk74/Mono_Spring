-- ============================================================
-- V13: Performance Indexes & Optimizations
-- ============================================================

-- products: filter is_active (mọi public query đều dùng)
ALTER TABLE products ADD INDEX idx_products_is_active (is_active);

-- products: composite index cho filter category + active
ALTER TABLE products ADD INDEX idx_products_cat_active (category_id, is_active);

-- orders: composite index cho list by user sorted by date (rất thường xuyên)
ALTER TABLE orders ADD INDEX idx_orders_user_date (user_id, created_at DESC);

-- orders: composite index cho admin filter by status + date
ALTER TABLE orders ADD INDEX idx_orders_status_date (status, created_at DESC);

-- reviews: standalone index cho product_id queries (thống kê, list)
-- (unique constraint (product_id, user_id) đã cover một phần nhưng không tối ưu cho count/avg)
ALTER TABLE reviews ADD INDEX idx_reviews_product (product_id);

-- order_items: composite index để support câu query check đã mua chưa
ALTER TABLE order_items ADD INDEX idx_order_items_sku_order (sku_id, order_id);
