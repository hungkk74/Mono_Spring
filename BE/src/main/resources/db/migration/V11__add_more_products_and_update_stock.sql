-- ============================================================
-- Mono Wear - V11: Add 20 more products and increase stock
-- ============================================================

-- Ensure categories exist (using existing ones or creating if missing)
INSERT INTO categories (name, slug, description, is_active, parent_id, created_at, updated_at) VALUES
('Áo Nam', 'ao-nam', 'Thời trang áo nam', true, NULL, NOW(), NOW()),
('Phụ Kiện', 'phu-kien', 'Phụ kiện thời trang', true, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

SET @cat_ao_nam = (SELECT id FROM categories WHERE slug = 'ao-nam' LIMIT 1);
SET @cat_giay_nam = (SELECT id FROM categories WHERE slug = 'giay-nam' LIMIT 1);
SET @cat_dong_ho = (SELECT id FROM categories WHERE slug = 'dong-ho' LIMIT 1);
SET @cat_phu_kien = (SELECT id FROM categories WHERE slug = 'phu-kien' LIMIT 1);
SET @cat_the_thao = (SELECT id FROM categories WHERE slug = 'quan-ao-the-thao' LIMIT 1);

-- 1. Insert 20 new products
INSERT INTO products (name, slug, material, description, image_url, is_active, category_id, created_at, updated_at) VALUES
('Áo Thun Cổ Tròn Xanh Navy', 'ao-thun-co-tron-xanh-navy', 'Cotton 100%', 'Áo thun cơ bản cổ tròn màu xanh navy.', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=900&q=80', true, @cat_ao_nam, NOW(), NOW()),
('Áo Polo Premium Trắng', 'ao-polo-premium-trang', 'Cá Sấu Cotton', 'Áo polo cao cấp màu trắng thanh lịch.', 'https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=900&q=80', true, @cat_ao_nam, NOW(), NOW()),
('Áo Sơ Mi Linen Nam', 'ao-so-mi-linen-nam', 'Linen', 'Áo sơ mi linen thoáng mát mùa hè.', 'https://images.unsplash.com/photo-1596755094514-f87e32f85e2c?w=900&q=80', true, @cat_ao_nam, NOW(), NOW()),
('Áo Khoác Bomber Đen', 'ao-khoac-bomber-den', 'Nylon', 'Áo khoác bomber đen hiện đại.', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=900&q=80', true, @cat_ao_nam, NOW(), NOW()),
('Áo Hoodie Basic Xám', 'ao-hoodie-basic-xam', 'Nỉ Cotton', 'Hoodie form rộng thoải mái màu xám.', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=900&q=80', true, @cat_ao_nam, NOW(), NOW()),
('Giày Oxford Da Đen', 'giay-oxford-da-den', 'Da bò thật', 'Giày Oxford nam màu đen sang trọng.', 'https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Chelsea Boot Nam', 'giay-chelsea-boot-nam', 'Da lộn', 'Giày Chelsea Boot phong cách cổ điển.', 'https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Sandal Nam Dạo Phố', 'giay-sandal-nam-dao-pho', 'Vải Canvas', 'Giày Sandal nam đi biển, dạo phố.', 'https://images.unsplash.com/photo-1603487742131-4160ec999306?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Lười Loafer Suede', 'giay-luoi-loafer-suede', 'Da Suede', 'Giày lười màu nâu bò.', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Giày Thể Thao Sneaker High-Top', 'giay-the-thao-sneaker-high-top', 'Da PU', 'Giày sneaker cổ cao thời thượng.', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=900&q=80', true, @cat_giay_nam, NOW(), NOW()),
('Đồng Hồ Cơ Lộ Máy Nam', 'dong-ho-co-lo-may-nam', 'Thép không gỉ', 'Đồng hồ cơ lộ máy skeleton nam tính.', 'https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Đồng Hồ Thông Minh Sport', 'dong-ho-thong-minh-sport', 'Silicone', 'Smartwatch kết nối bluetooth.', 'https://images.unsplash.com/photo-1523170335258-f5ed11844a49?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Đồng Hồ Dây Da Cổ Điển Nữ', 'dong-ho-day-da-co-dien-nu', 'Dây da tự nhiên', 'Đồng hồ nữ mặt tròn vintage.', 'https://images.unsplash.com/photo-1539874754764-5a96559165b0?w=900&q=80', true, @cat_dong_ho, NOW(), NOW()),
('Mắt Kính Râm Gọng Vuông', 'mat-kinh-ram-gong-vuong', 'Nhựa Acetate', 'Kính râm gọng vuông màu đen nam tính.', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Thắt Lưng Da Thật Đen', 'that-lung-da-that-den', 'Da bò thật', 'Thắt lưng nam khóa kim loại.', 'https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Ví Da Nam Đứng', 'vi-da-nam-dung', 'Da Microfiber', 'Ví da nam kiểu đứng, nhiều ngăn tiện lợi.', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Balo Nam Chống Nước', 'balo-nam-chong-nuoc', 'Oxford Cloth', 'Balo laptop nam chống nước.', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Túi Đeo Chéo Messenger', 'tui-deo-cheo-messenger', 'Canvas', 'Túi đeo chéo vải canvas bụi bặm.', 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Nón Lưỡi Trai Logo Trắng', 'non-luoi-trai-logo-trang', 'Cotton', 'Nón kết form đẹp thêu logo mỏ neo.', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=900&q=80', true, @cat_phu_kien, NOW(), NOW()),
('Áo Ba Lỗ Tập Gym', 'ao-ba-lo-tap-gym', 'Mesh Polyester', 'Áo ba lỗ thể thao tập gym.', 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=900&q=80', true, @cat_the_thao, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    name = VALUES(name), description = VALUES(description), image_url = VALUES(image_url), updated_at = NOW();

-- 2. Insert corresponding SKUs for these new products (initial stock 50)
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('NEW20-', p.slug, '-L'), 'L', 'Chuẩn', 299000, 50, true, p.id, NOW(), NOW()
FROM products p
WHERE p.slug IN (
  'ao-thun-co-tron-xanh-navy', 'ao-polo-premium-trang', 'ao-so-mi-linen-nam', 'ao-khoac-bomber-den', 'ao-hoodie-basic-xam',
  'giay-oxford-da-den', 'giay-chelsea-boot-nam', 'giay-sandal-nam-dao-pho', 'giay-luoi-loafer-suede', 'giay-the-thao-sneaker-high-top',
  'dong-ho-co-lo-may-nam', 'dong-ho-thong-minh-sport', 'dong-ho-day-da-co-dien-nu', 'mat-kinh-ram-gong-vuong', 'that-lung-da-that-den',
  'vi-da-nam-dung', 'balo-nam-chong-nuoc', 'tui-deo-cheo-messenger', 'non-luoi-trai-logo-trang', 'ao-ba-lo-tap-gym'
)
ON DUPLICATE KEY UPDATE stock = VALUES(stock);

-- 3. Increase stock of ALL existing SKUs by 40
UPDATE skus SET stock = stock + 40, updated_at = NOW();
