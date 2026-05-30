-- ============================================================
-- Mono Wear - V12: Fix categories for new products
-- ============================================================

UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'giay-nam' LIMIT 1)
WHERE slug IN ('giay-oxford-da-den', 'giay-chelsea-boot-nam', 'giay-sandal-nam-dao-pho', 'giay-luoi-loafer-suede', 'giay-the-thao-sneaker-high-top');

UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'dong-ho' LIMIT 1)
WHERE slug IN ('dong-ho-co-lo-may-nam', 'dong-ho-thong-minh-sport', 'dong-ho-day-da-co-dien-nu');

UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'phu-kien' LIMIT 1)
WHERE slug IN ('mat-kinh-ram-gong-vuong', 'that-lung-da-that-den', 'vi-da-nam-dung', 'balo-nam-chong-nuoc', 'tui-deo-cheo-messenger', 'non-luoi-trai-logo-trang');

UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'ao-nam' LIMIT 1)
WHERE slug IN ('ao-thun-co-tron-xanh-navy', 'ao-polo-premium-trang', 'ao-so-mi-linen-nam', 'ao-khoac-bomber-den', 'ao-hoodie-basic-xam');

UPDATE products 
SET category_id = (SELECT id FROM categories WHERE slug = 'quan-ao-the-thao' LIMIT 1)
WHERE slug IN ('ao-ba-lo-tap-gym');
