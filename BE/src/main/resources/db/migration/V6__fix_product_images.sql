-- V6: Fix broken product image URLs
-- Replace Unsplash URLs that may 404 with verified working alternatives

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800&q=80' WHERE slug = 'ao-polo-xanh-reu';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=800&q=80' WHERE slug = 'ao-thun-basic-den';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800&q=80' WHERE slug = 'quan-chinos-kaki';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1598032895397-b9472444bf93?w=800&q=80' WHERE slug = 'ao-so-mi-trang-linen';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800&q=80' WHERE slug = 'ao-khoac-bomber-da';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1562157873-818bc0726f68?w=800&q=80' WHERE slug = 'ao-thun-ke-soc-ngang';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&q=80' WHERE slug = 'ao-khoac-ni-hoodie';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1604176354204-9268737828e4?w=800&q=80' WHERE slug = 'quan-jean-xanh-dam';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1614975059251-992f11792571?w=800&q=80' WHERE slug = 'ao-len-co-lo-nau';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80' WHERE slug = 'ao-cardigan-xam';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80' WHERE slug = 'quan-short-the-thao-den';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=800&q=80' WHERE slug = 'ao-so-mi-ke-caro';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1529374255404-311a2a4f1fd9?w=800&q=80' WHERE slug = 'ao-thun-graphic-trang';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80' WHERE slug = 'ao-so-mi-oxford-xanh';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1618517351600-b6cb5f28c50c?w=800&q=80' WHERE slug = 'ao-thun-dai-tay-den';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=800&q=80' WHERE slug = 'ao-hoodie-trang-kem';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800&q=80' WHERE slug = 'ao-khoac-gio-den';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1565084888279-aca5ecc8f8c5?w=800&q=80' WHERE slug = 'quan-short-kaki-xam';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80' WHERE slug = 'quan-tay-ong-rong-den';
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=800&q=80' WHERE slug = 'ao-denim-jacket-xanh-bac';

-- Also fix original products that might be missing image_url
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1625910513413-5fc421e0b6e3?w=800&q=80' WHERE slug = 'ao-polo-pima-cotton' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80' WHERE slug = 'ao-thun-organic-basic' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&q=80' WHERE slug = 'quan-tay-slim-fit' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1507680434267-325608a91470?w=800&q=80' WHERE slug = 'ao-khoac-minimal-blazer' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800&q=80' WHERE slug = 'ao-polo-pique-classic' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=800&q=80' WHERE slug = 'ao-polo-slim-fit-pro' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800&q=80' WHERE slug = 'ao-polo-minimalist-texture' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1622445275576-721325763afe?w=800&q=80' WHERE slug = 'ao-polo-air-flow-white' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&q=80' WHERE slug = 'ao-polo-oxford-premium' AND (image_url IS NULL OR image_url = '');
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1598033129183-c4f50c736c10?w=800&q=80' WHERE slug = 'ao-polo-urban-charcoal' AND (image_url IS NULL OR image_url = '');
