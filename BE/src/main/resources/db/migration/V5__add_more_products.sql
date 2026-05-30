SET @cat_polo = (SELECT id FROM categories WHERE slug = 'ao-polo');
SET @cat_thun = (SELECT id FROM categories WHERE slug = 'ao-thun');
SET @cat_khoac = (SELECT id FROM categories WHERE slug = 'ao-khoac');
SET @cat_qtay = (SELECT id FROM categories WHERE slug = 'quan-tay');
SET @cat_qshort = (SELECT id FROM categories WHERE slug = 'quan-short');
SET @cat_pk = (SELECT id FROM categories WHERE slug = 'phu-kien');

INSERT INTO products (name, slug, material, description, image_url, is_active, category_id, created_at, updated_at) VALUES
('Áo Polo Xanh Rêu', 'ao-polo-xanh-reu', 'Cotton Pique', 'Áo polo xanh rêu trầm, thanh lịch.', 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800&q=80', true, @cat_polo, NOW(), NOW()),
('Áo Thun Basic Đen', 'ao-thun-basic-den', 'Organic Cotton', 'Áo thun đen tối giản, dễ phối.', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Quần Chinos Kaki', 'quan-chinos-kaki', 'Kaki', 'Quần Kaki form đứng form, thanh lịch.', 'https://images.unsplash.com/photo-1624378439575-d1eaa6ada46f?w=800&q=80', true, @cat_qtay, NOW(), NOW()),
('Áo Sơ Mi Trắng Linen', 'ao-so-mi-trang-linen', 'Linen', 'Áo sơ mi linen thoáng mát, nam tính.', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Khoác Bomber Da', 'ao-khoac-bomber-da', 'Da lộn', 'Áo khoác bomber da sang trọng.', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80', true, @cat_khoac, NOW(), NOW()),
('Áo Thun Kẻ Sọc Ngang', 'ao-thun-ke-soc-ngang', 'Cotton', 'Áo thun kẻ sọc phong cách.', 'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Khoác Nỉ Hoodie', 'ao-khoac-ni-hoodie', 'Nỉ Cotton', 'Áo hoodie nỉ dày, ấm áp.', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800&q=80', true, @cat_khoac, NOW(), NOW()),
('Quần Jean Xanh Đậm', 'quan-jean-xanh-dam', 'Denim', 'Quần Jean ống suông cổ điển.', 'https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=800&q=80', true, @cat_qtay, NOW(), NOW()),
('Áo Len Cổ Lọ Nâu', 'ao-len-co-lo-nau', 'Len', 'Áo len cổ lọ nam tính, giữ nhiệt.', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Cardigan Xám', 'ao-cardigan-xam', 'Len', 'Áo Cardigan mỏng, thanh nhã.', 'https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=800&q=80', true, @cat_khoac, NOW(), NOW()),
('Quần Short Thể Thao Đen', 'quan-short-the-thao-den', 'Poly', 'Quần short siêu nhẹ, thoáng mát.', 'https://images.unsplash.com/photo-1550995694-3f5f4a7e1bd2?w=800&q=80', true, @cat_qshort, NOW(), NOW()),
('Áo Sơ Mi Kẻ Caro', 'ao-so-mi-ke-caro', 'Cotton', 'Áo sơ mi caro đỏ đen trẻ trung.', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Thun Graphic Trắng', 'ao-thun-graphic-trang', 'Cotton', 'Áo thun in hình Graphic.', 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Sơ Mi Oxford Xanh', 'ao-so-mi-oxford-xanh', 'Oxford Cotton', 'Áo sơ mi Oxford thanh lịch, bền bỉ.', 'https://images.unsplash.com/photo-1505022610485-0249ba5b3675?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Thun Dài Tay Đen', 'ao-thun-dai-tay-den', 'Cotton', 'Áo thun dài tay cổ điển.', 'https://images.unsplash.com/photo-1516826957135-700ede19ebc1?w=800&q=80', true, @cat_thun, NOW(), NOW()),
('Áo Hoodie Trắng Kem', 'ao-hoodie-trang-kem', 'Nỉ', 'Áo Hoodie form rộng năng động.', 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80', true, @cat_khoac, NOW(), NOW()),
('Áo Khoác Gió Đen', 'ao-khoac-gio-den', 'Poly', 'Áo khoác gió siêu nhẹ, cản gió tốt.', 'https://images.unsplash.com/photo-1507680434267-325608a91470?w=800&q=80', true, @cat_khoac, NOW(), NOW()),
('Quần Short Kaki Xám', 'quan-short-kaki-xam', 'Kaki', 'Quần short kaki dày dặn.', 'https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800&q=80', true, @cat_qshort, NOW(), NOW()),
('Quần Tây Ống Rộng Đen', 'quan-tay-ong-rong-den', 'Tuyết Mưa', 'Quần tây ống rộng phong cách Hàn Quốc.', 'https://images.unsplash.com/photo-1618517351600-b6cb5f28c50c?w=800&q=80', true, @cat_qtay, NOW(), NOW()),
('Áo Denim Jacket Xanh Bạc', 'ao-denim-jacket-xanh-bac', 'Denim', 'Áo Denim khoác ngoài bụi bặm.', 'https://images.unsplash.com/photo-1485230895905-eb56b6c09b52?w=800&q=80', true, @cat_khoac, NOW(), NOW());

-- Thêm SKUs cho 20 sản phẩm vừa tạo
INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('SKU-', id, '-M'), 'M', 'Đen', 350000, 50, true, id, NOW(), NOW() FROM products ORDER BY id DESC LIMIT 20;

INSERT INTO skus (sku_code, size, color, price, stock, is_active, product_id, created_at, updated_at)
SELECT CONCAT('SKU-', id, '-L'), 'L', 'Trắng', 350000, 50, true, id, NOW(), NOW() FROM products ORDER BY id DESC LIMIT 20;
