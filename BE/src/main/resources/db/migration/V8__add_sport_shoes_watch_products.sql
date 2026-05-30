-- ============================================================
-- Mono Wear - V8: Add sportswear, shoes, and watch catalog
-- ============================================================

INSERT INTO categories (name, slug, description, is_active, parent_id, created_at, updated_at) VALUES
('Quần Áo Thể Thao', 'quan-ao-the-thao', 'Trang phục thể thao nam nữ, tối giản và linh hoạt', true, NULL, NOW(), NOW()),
('Giày Nam', 'giay-nam', 'Giày nam sneaker, running và smart casual', true, NULL, NOW(), NOW()),
('Giày Nữ', 'giay-nu', 'Giày nữ sneaker, running và sandal tối giản', true, NULL, NOW(), NOW()),
('Đồng Hồ', 'dong-ho', 'Đồng hồ thời trang nam nữ', true, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    is_active = true,
    updated_at = NOW();

SET @cat_sport = (SELECT id FROM categories WHERE slug = 'quan-ao-the-thao');
SET @cat_giay_nam = (SELECT id FROM categories WHERE slug = 'giay-nam');
SET @cat_giay_nu = (SELECT id FROM categories WHERE slug = 'giay-nu');
SET @cat_dong_ho = (SELECT id FROM categories WHERE slug = 'dong-ho');

INSERT INTO products (name, slug, material, description, image_url, is_active, category_id, created_at, updated_at) VALUES
('Áo Thể Thao Dry-Fit Đen', 'ao-the-thao-dry-fit-den', 'Polyester Dry-Fit', 'Áo thể thao thoáng khí, thấm hút nhanh, phù hợp tập luyện hằng ngày.', 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=900&q=80', true, @cat_sport, NOW(), NOW()),
('Quần Jogger Tech Knit', 'quan-jogger-tech-knit', 'Tech Knit Stretch', 'Quần jogger co giãn, nhẹ và giữ form tốt cho vận động.', 'https://images.unsplash.com/photo-1506629905607-d9c297d9f5f5?w=900&q=80', true, @cat_sport, NOW(), NOW()),
('Áo Khoác Gió Training', 'ao-khoac-gio-training', 'Nylon chống gió', 'Áo khoác gió thể thao nhẹ, gọn, chống gió tốt khi di chuyển ngoài trời.', 'https://images.unsplash.com/photo-1507680434267-325608a91470?w=900&q=80', true, @cat_sport, NOW(), NOW()),
('Set Thể Thao Minimal Grey', 'set-the-thao-minimal-grey', 'Cotton Poly Blend', 'Set áo quần thể thao màu xám tối giản, mặc tập hoặc casual đều phù hợp.', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=900&q=80', true, @cat_sport, NOW(), NOW()),

('Giày Sneaker Nam Trắng', 'giay-sneaker-nam-trang', 'Da tổng hợp cao cấp', 'Sneaker nam trắng tối giản, dễ phối với quần jeans, chinos và short.', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Running Nam Đen', 'giay-running-nam-den', 'Mesh thoáng khí', 'Giày running nam đệm nhẹ, bám đường tốt cho chạy bộ và tập gym.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Loafer Nam Nâu', 'giay-loafer-nam-nau', 'Da microfiber', 'Loafer nam màu nâu, phom thanh lịch cho smart casual và công sở.', 'https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Derby Nam Classic', 'giay-derby-nam-classic', 'Da PU phủ mờ', 'Derby nam cổ điển, đường may gọn và đế chắc chắn.', 'https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),

('Giày Sneaker Nữ Pastel', 'giay-sneaker-nu-pastel', 'Da tổng hợp mềm', 'Sneaker nữ tone pastel nhẹ nhàng, phù hợp đi học, đi làm và dạo phố.', 'https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=900&q=80', true, @cat_giay_nu, NOW(), NOW()),
('Giày Running Nữ Trắng', 'giay-running-nu-trang', 'Mesh và EVA', 'Giày running nữ trắng, nhẹ chân và thoáng khí khi vận động.', 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=900&q=80', true, @cat_giay_nu, NOW(), NOW()),
('Giày Mule Nữ Minimal', 'giay-mule-nu-minimal', 'Da mềm', 'Mule nữ tối giản, dễ mang và phù hợp phong cách thanh lịch.', 'https://images.unsplash.com/photo-1562273138-f46be4ebdf33?w=900&q=80', true, @cat_giay_nu, NOW(), NOW()),
('Sandal Nữ Leather Strap', 'sandal-nu-leather-strap', 'Da tổng hợp', 'Sandal nữ quai mảnh, nhẹ và phù hợp thời tiết nóng.', 'https://images.unsplash.com/photo-1603487742131-4160ec999306?w=900&q=80', true, @cat_giay_nu, NOW(), NOW()),

('Đồng Hồ Automatic Đen', 'dong-ho-automatic-den', 'Thép không gỉ', 'Đồng hồ mặt đen tối giản, máy automatic, dây thép chắc chắn.', 'https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Đồng Hồ Mesh Silver', 'dong-ho-mesh-silver', 'Thép mesh', 'Đồng hồ dây mesh bạc, mặt mỏng và thanh lịch.', 'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Đồng Hồ Sport Chrono', 'dong-ho-sport-chrono', 'Thép và silicone', 'Đồng hồ chronograph thể thao, mặt lớn và dây silicone bền.', 'https://images.unsplash.com/photo-1434056886845-dac89ffe9b56?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Đồng Hồ Leather Brown', 'dong-ho-leather-brown', 'Dây da nâu', 'Đồng hồ dây da nâu, thiết kế cổ điển và dễ phối đồ.', 'https://images.unsplash.com/photo-1539874754764-5a96559165b0?w=900&q=80', true, @cat_dong_ho, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    material = VALUES(material),
    description = VALUES(description),
    image_url = VALUES(image_url),
    is_active = true,
    category_id = VALUES(category_id),
    updated_at = NOW();

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('SPORT-', p.slug, '-M'), 'M', 'Đen', 390000, 40, true, p.id, NOW(), NOW()
FROM products p
WHERE p.slug IN ('ao-the-thao-dry-fit-den', 'quan-jogger-tech-knit', 'ao-khoac-gio-training', 'set-the-thao-minimal-grey')
ON DUPLICATE KEY UPDATE price = VALUES(price), stock = VALUES(stock), is_active = true, updated_at = NOW();

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('MENSHOE-', p.slug, '-41'), '41', 'Trắng', 790000, 35, true, p.id, NOW(), NOW()
FROM products p
WHERE p.slug IN ('giay-sneaker-nam-trang', 'giay-running-nam-den', 'giay-loafer-nam-nau', 'giay-derby-nam-classic')
ON DUPLICATE KEY UPDATE price = VALUES(price), stock = VALUES(stock), is_active = true, updated_at = NOW();

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('WOMENSHOE-', p.slug, '-38'), '38', 'Trắng', 690000, 35, true, p.id, NOW(), NOW()
FROM products p
WHERE p.slug IN ('giay-sneaker-nu-pastel', 'giay-running-nu-trang', 'giay-mule-nu-minimal', 'sandal-nu-leather-strap')
ON DUPLICATE KEY UPDATE price = VALUES(price), stock = VALUES(stock), is_active = true, updated_at = NOW();

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('WATCH-', p.slug, '-STD'), 'FREE', 'Bạc', 1290000, 25, true, p.id, NOW(), NOW()
FROM products p
WHERE p.slug IN ('dong-ho-automatic-den', 'dong-ho-mesh-silver', 'dong-ho-sport-chrono', 'dong-ho-leather-brown')
ON DUPLICATE KEY UPDATE price = VALUES(price), stock = VALUES(stock), is_active = true, updated_at = NOW();
