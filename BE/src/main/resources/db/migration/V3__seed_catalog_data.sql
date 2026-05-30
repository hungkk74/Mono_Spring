-- V3: Seed danh mục + sản phẩm + SKU cho Mono Wear (matching FE)

-- Sub-categories cho "Ao" (id=1)
INSERT INTO categories (name, slug, description, is_active, parent_id, created_at, updated_at) VALUES
('Áo Polo', 'ao-polo', 'Áo polo nam cao cấp', true, 1, NOW(), NOW()),
('Áo Thun', 'ao-thun', 'Áo thun nam basic và premium', true, 1, NOW(), NOW()),
('Áo Khoác', 'ao-khoac', 'Áo khoác nam minimal', true, 1, NOW(), NOW());

-- Root categories
INSERT INTO categories (name, slug, description, is_active, parent_id, created_at, updated_at) VALUES
('Quần', 'quan', 'Tất cả các loại quần nam', true, NULL, NOW(), NOW()),
('Phụ Kiện', 'phu-kien', 'Phụ kiện thời trang nam', true, NULL, NOW(), NOW());

-- Sub for Quần - use SET variable workaround for MySQL
SET @quan_id = (SELECT id FROM categories WHERE slug = 'quan');
INSERT INTO categories (name, slug, description, is_active, parent_id, created_at, updated_at) VALUES
('Quần Tây', 'quan-tay', 'Quần tây nam công sở', true, @quan_id, NOW(), NOW()),
('Quần Short', 'quan-short', 'Quần short nam thời trang', true, @quan_id, NOW(), NOW());

-- ==================== PRODUCTS ====================
SET @cat_polo = (SELECT id FROM categories WHERE slug = 'ao-polo');
SET @cat_thun = (SELECT id FROM categories WHERE slug = 'ao-thun');
SET @cat_khoac = (SELECT id FROM categories WHERE slug = 'ao-khoac');
SET @cat_qtay = (SELECT id FROM categories WHERE slug = 'quan-tay');

INSERT INTO products (name, slug, material, description, is_active, category_id, created_at, updated_at) VALUES
('Áo Polo Pima Cotton', 'ao-polo-pima-cotton', 'Pima Cotton 100%', 'Áo polo nam Pima Cotton cao cấp, mềm mại, thoáng khí. Form Regular Fit.', true, @cat_polo, NOW(), NOW()),
('Áo Thun Organic Basic', 'ao-thun-organic-basic', 'Organic Cotton 100%', 'Áo thun nam basic cotton hữu cơ. Form Relaxed Fit, cổ tròn classic.', true, @cat_thun, NOW(), NOW()),
('Quần Tây Slim-Fit', 'quan-tay-slim-fit', 'Polyester Blend 65/35', 'Quần tây nam Slim-Fit thanh lịch, giữ phom tốt.', true, @cat_qtay, NOW(), NOW()),
('Áo Khoác Minimal Blazer', 'ao-khoac-minimal-blazer', 'Cotton Heavy-weight', 'Blazer nam tối giản, cotton dày dặn. Form Tailored Fit.', true, @cat_khoac, NOW(), NOW()),
('Áo Polo Pique Classic', 'ao-polo-pique-classic', 'Cotton Pique', 'Áo polo Pique dệt kim cao cấp. Form Slim Fit, cổ bẻ cứng cáp.', true, @cat_polo, NOW(), NOW()),
('Áo Polo Slim Fit Pro', 'ao-polo-slim-fit-pro', 'Cotton Stretch', 'Áo polo performance cotton pha spandex. Form Slim Fit.', true, @cat_polo, NOW(), NOW()),
('Áo Polo Minimalist Texture', 'ao-polo-minimalist-texture', 'Cotton Jacquard', 'Áo polo texture jacquard tinh tế. Form Regular Fit.', true, @cat_polo, NOW(), NOW()),
('Áo Polo Air Flow White', 'ao-polo-air-flow-white', 'Micro-fiber Breathable', 'Áo polo trắng micro-fiber siêu thoáng. Placket ẩn hiện đại.', true, @cat_polo, NOW(), NOW()),
('Áo Polo Oxford Premium', 'ao-polo-oxford-premium', 'Cotton Oxford', 'Áo polo Oxford basket weave. Form Tailored Fit.', true, @cat_polo, NOW(), NOW()),
('Áo Polo Urban Charcoal', 'ao-polo-urban-charcoal', 'Cotton Blend', 'Áo polo xám charcoal phong cách urban. Form Modern Fit.', true, @cat_polo, NOW(), NOW());

-- ==================== SKUs ====================
SET @p1 = (SELECT id FROM products WHERE slug = 'ao-polo-pima-cotton');
SET @p2 = (SELECT id FROM products WHERE slug = 'ao-thun-organic-basic');
SET @p3 = (SELECT id FROM products WHERE slug = 'quan-tay-slim-fit');
SET @p4 = (SELECT id FROM products WHERE slug = 'ao-khoac-minimal-blazer');
SET @p5 = (SELECT id FROM products WHERE slug = 'ao-polo-pique-classic');
SET @p6 = (SELECT id FROM products WHERE slug = 'ao-polo-slim-fit-pro');
SET @p7 = (SELECT id FROM products WHERE slug = 'ao-polo-minimalist-texture');
SET @p8 = (SELECT id FROM products WHERE slug = 'ao-polo-air-flow-white');
SET @p9 = (SELECT id FROM products WHERE slug = 'ao-polo-oxford-premium');
SET @p10 = (SELECT id FROM products WHERE slug = 'ao-polo-urban-charcoal');

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at) VALUES
('PIMA-BLK-M','M','Đen',299000,50,true,@p1,NOW(),NOW()),
('PIMA-BLK-L','L','Đen',299000,45,true,@p1,NOW(),NOW()),
('PIMA-WHT-M','M','Trắng',299000,40,true,@p1,NOW(),NOW()),
('PIMA-WHT-L','L','Trắng',299000,35,true,@p1,NOW(),NOW()),
('ORG-WHT-M','M','Trắng',199000,80,true,@p2,NOW(),NOW()),
('ORG-WHT-L','L','Trắng',199000,70,true,@p2,NOW(),NOW()),
('ORG-BLK-M','M','Đen',199000,60,true,@p2,NOW(),NOW()),
('ORG-BLK-L','L','Đen',199000,55,true,@p2,NOW(),NOW()),
('SLIM-NVY-30','30','Navy',550000,30,true,@p3,NOW(),NOW()),
('SLIM-NVY-32','32','Navy',550000,35,true,@p3,NOW(),NOW()),
('SLIM-BLK-30','30','Đen',550000,25,true,@p3,NOW(),NOW()),
('SLIM-BLK-32','32','Đen',550000,28,true,@p3,NOW(),NOW()),
('BLZR-BLK-M','M','Đen',1250000,15,true,@p4,NOW(),NOW()),
('BLZR-BLK-L','L','Đen',1250000,12,true,@p4,NOW(),NOW()),
('BLZR-GRY-M','M','Xám',1250000,10,true,@p4,NOW(),NOW()),
('PIQ-BLK-M','M','Đen',299000,60,true,@p5,NOW(),NOW()),
('PIQ-BLK-L','L','Đen',299000,50,true,@p5,NOW(),NOW()),
('PIQ-WHT-M','M','Trắng',299000,55,true,@p5,NOW(),NOW()),
('SFP-NVY-M','M','Navy',349000,45,true,@p6,NOW(),NOW()),
('SFP-NVY-L','L','Navy',349000,40,true,@p6,NOW(),NOW()),
('SFP-GRY-M','M','Xám',349000,38,true,@p6,NOW(),NOW()),
('MTX-GRY-M','M','Xám nhạt',450000,30,true,@p7,NOW(),NOW()),
('MTX-GRY-L','L','Xám nhạt',450000,25,true,@p7,NOW(),NOW()),
('MTX-BLK-M','M','Đen',450000,28,true,@p7,NOW(),NOW()),
('AFW-WHT-M','M','Trắng',399000,40,true,@p8,NOW(),NOW()),
('AFW-WHT-L','L','Trắng',399000,35,true,@p8,NOW(),NOW()),
('AFW-WHT-XL','XL','Trắng',399000,30,true,@p8,NOW(),NOW()),
('OXF-NVY-M','M','Navy',550000,20,true,@p9,NOW(),NOW()),
('OXF-NVY-L','L','Navy',550000,18,true,@p9,NOW(),NOW()),
('OXF-GRY-M','M','Xám',550000,22,true,@p9,NOW(),NOW()),
('URB-CHR-M','M','Charcoal',420000,35,true,@p10,NOW(),NOW()),
('URB-CHR-L','L','Charcoal',420000,30,true,@p10,NOW(),NOW()),
('URB-CHR-XL','XL','Charcoal',420000,25,true,@p10,NOW(),NOW());
