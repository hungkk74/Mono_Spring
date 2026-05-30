-- Add product sale metadata and checkout coupon support

ALTER TABLE products
    ADD COLUMN sale_percent INT NULL AFTER image_url,
    ADD COLUMN sale_start_at TIMESTAMP NULL AFTER sale_percent,
    ADD COLUMN sale_end_at TIMESTAMP NULL AFTER sale_start_at;

ALTER TABLE orders
    ADD COLUMN subtotal_amount DECIMAL(15,2) NULL AFTER status,
    ADD COLUMN discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0 AFTER subtotal_amount,
    ADD COLUMN coupon_code VARCHAR(50) NULL AFTER discount_amount;

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount IS NULL;

ALTER TABLE orders
    MODIFY subtotal_amount DECIMAL(15,2) NOT NULL;

CREATE TABLE coupons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(15,2) NOT NULL,
    max_discount_amount DECIMAL(15,2) NULL,
    min_order_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    usage_limit INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    start_at TIMESTAMP NULL,
    end_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_coupons_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO coupons (code, description, discount_type, discount_value, max_discount_amount, min_order_amount, usage_limit, is_active, start_at, end_at)
VALUES
('MONO10', 'Giảm 10% cho đơn từ 300.000đ', 'PERCENT', 10, 100000, 300000, 500, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY)),
('WELCOME50', 'Giảm 50.000đ cho đơn đầu mùa', 'FIXED', 50000, NULL, 500000, 300, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 180 DAY))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    discount_type = VALUES(discount_type),
    discount_value = VALUES(discount_value),
    max_discount_amount = VALUES(max_discount_amount),
    min_order_amount = VALUES(min_order_amount),
    usage_limit = VALUES(usage_limit),
    is_active = TRUE,
    updated_at = NOW();

UPDATE products
SET sale_percent = 20, sale_start_at = NOW(), sale_end_at = DATE_ADD(NOW(), INTERVAL 90 DAY)
WHERE slug IN ('ao-polo-pima-cotton', 'ao-thun-organic-basic', 'ao-polo-pique-classic', 'giay-sneaker-nam-trang', 'dong-ho-mesh-silver');

UPDATE products
SET sale_percent = 15, sale_start_at = NOW(), sale_end_at = DATE_ADD(NOW(), INTERVAL 90 DAY)
WHERE slug IN ('quan-chinos-kaki', 'ao-khoac-gio-training', 'giay-running-nu-trang', 'set-the-thao-minimal-grey');
