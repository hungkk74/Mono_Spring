-- V14: Thêm nhiều biến thể khác (Size, Màu sắc) cho các sản phẩm hiện có

-- Lấy ID của các sản phẩm hiện có
SET @p_pima_cotton = (SELECT id FROM products WHERE slug = 'ao-polo-pima-cotton');
SET @p_organic_basic = (SELECT id FROM products WHERE slug = 'ao-thun-organic-basic');
SET @p_quan_tay = (SELECT id FROM products WHERE slug = 'quan-tay-slim-fit');
SET @p_blazer = (SELECT id FROM products WHERE slug = 'ao-khoac-minimal-blazer');
SET @p_pique = (SELECT id FROM products WHERE slug = 'ao-polo-pique-classic');
SET @p_slim_fit_pro = (SELECT id FROM products WHERE slug = 'ao-polo-slim-fit-pro');

-- 1. Thêm biến thể cho Áo Polo Pima Cotton (pima-cotton)
-- Thêm size XL cho Đen và Trắng
-- Thêm màu Navy mới (M, L, XL)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('PIMA-BLK-XL', 'XL', 'Đen', 299000, 30, true, @p_pima_cotton, NOW(), NOW()),
('PIMA-WHT-XL', 'XL', 'Trắng', 299000, 25, true, @p_pima_cotton, NOW(), NOW()),
('PIMA-NVY-M', 'M', 'Navy', 299000, 40, true, @p_pima_cotton, NOW(), NOW()),
('PIMA-NVY-L', 'L', 'Navy', 299000, 35, true, @p_pima_cotton, NOW(), NOW()),
('PIMA-NVY-XL', 'XL', 'Navy', 299000, 30, true, @p_pima_cotton, NOW(), NOW());

-- 2. Thêm biến thể cho Áo Thun Organic Basic (organic-basic)
-- Thêm size XL cho Đen và Trắng
-- Thêm màu Xám nhạt mới (M, L, XL)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('ORG-WHT-XL', 'XL', 'Trắng', 199000, 50, true, @p_organic_basic, NOW(), NOW()),
('ORG-BLK-XL', 'XL', 'Đen', 199000, 45, true, @p_organic_basic, NOW(), NOW()),
('ORG-GRY-M', 'M', 'Xám nhạt', 199000, 60, true, @p_organic_basic, NOW(), NOW()),
('ORG-GRY-L', 'L', 'Xám nhạt', 199000, 55, true, @p_organic_basic, NOW(), NOW()),
('ORG-GRY-XL', 'XL', 'Xám nhạt', 199000, 40, true, @p_organic_basic, NOW(), NOW());

-- 3. Thêm biến thể cho Quần Tây Slim-Fit (quan-tay-slim-fit)
-- Thêm size 31, 33 cho Navy và Đen
-- Thêm màu Xám đậm mới (30, 31, 32, 33)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('SLIM-NVY-31', '31', 'Navy', 550000, 20, true, @p_quan_tay, NOW(), NOW()),
('SLIM-NVY-33', '33', 'Navy', 550000, 22, true, @p_quan_tay, NOW(), NOW()),
('SLIM-BLK-31', '31', 'Đen', 550000, 18, true, @p_quan_tay, NOW(), NOW()),
('SLIM-BLK-33', '33', 'Đen', 550000, 15, true, @p_quan_tay, NOW(), NOW()),
('SLIM-GRY-30', '30', 'Xám đậm', 550000, 25, true, @p_quan_tay, NOW(), NOW()),
('SLIM-GRY-31', '31', 'Xám đậm', 550000, 20, true, @p_quan_tay, NOW(), NOW()),
('SLIM-GRY-32', '32', 'Xám đậm', 550000, 25, true, @p_quan_tay, NOW(), NOW()),
('SLIM-GRY-33', '33', 'Xám đậm', 550000, 20, true, @p_quan_tay, NOW(), NOW());

-- 4. Thêm biến thể cho Áo Khoác Minimal Blazer (ao-khoac-minimal-blazer)
-- Thêm size XL cho Đen
-- Thêm size L, XL cho Xám
-- Thêm màu Be mới (M, L)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('BLZR-BLK-XL', 'XL', 'Đen', 1250000, 10, true, @p_blazer, NOW(), NOW()),
('BLZR-GRY-L', 'L', 'Xám', 1250000, 8, true, @p_blazer, NOW(), NOW()),
('BLZR-GRY-XL', 'XL', 'Xám', 1250000, 5, true, @p_blazer, NOW(), NOW()),
('BLZR-BEG-M', 'M', 'Beige', 1250000, 12, true, @p_blazer, NOW(), NOW()),
('BLZR-BEG-L', 'L', 'Beige', 1250000, 10, true, @p_blazer, NOW(), NOW());

-- 5. Thêm biến thể cho Áo Polo Pique Classic (ao-polo-pique-classic)
-- Thêm size L cho Trắng
-- Thêm màu Xanh Rêu mới (M, L)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('PIQ-WHT-L', 'L', 'Trắng', 299000, 45, true, @p_pique, NOW(), NOW()),
('PIQ-GRN-M', 'M', 'Xanh rêu', 299000, 40, true, @p_pique, NOW(), NOW()),
('PIQ-GRN-L', 'L', 'Xanh rêu', 299000, 35, true, @p_pique, NOW(), NOW());

-- 6. Thêm biến thể cho Áo Polo Slim Fit Pro (ao-polo-slim-fit-pro)
-- Thêm size L cho Xám
-- Thêm màu Đỏ Burgundy mới (M, L)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('SFP-GRY-L', 'L', 'Xám', 349000, 30, true, @p_slim_fit_pro, NOW(), NOW()),
('SFP-BGD-M', 'M', 'Burgundy', 349000, 35, true, @p_slim_fit_pro, NOW(), NOW()),
('SFP-BGD-L', 'L', 'Burgundy', 349000, 30, true, @p_slim_fit_pro, NOW(), NOW());
