-- Banner table for homepage hero slides (supports IMAGE and VIDEO)
CREATE TABLE banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500),
    media_url VARCHAR(500) NOT NULL,
    media_type VARCHAR(10) NOT NULL DEFAULT 'IMAGE',
    thumbnail_url VARCHAR(500),
    link_url VARCHAR(500),
    cta_text VARCHAR(100),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_banners_active_order (is_active, display_order)
);

-- Seed data: migrate existing hardcoded HERO_SLIDES from FE
INSERT INTO banners (title, subtitle, media_url, media_type, cta_text, link_url, display_order) VALUES
('BỘ SƯU TẬP MỚI 2026', 'Phong cách tối giản - Chất lượng vượt trội',
 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1920&q=90&fit=crop&crop=center',
 'IMAGE', 'KHÁM PHÁ NGAY', '/shop', 1),
('ESSENTIAL MENSWEAR', 'Những thiết kế nền tảng cho tủ đồ hiện đại',
 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1920&q=90&fit=crop&crop=center',
 'IMAGE', 'XEM BỘ SƯU TẬP', '/shop', 2),
('SMART CASUAL', 'Lịch sự, gọn gàng và linh hoạt trong mọi lịch trình',
 'https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1920&q=90&fit=crop&crop=center',
 'IMAGE', 'MUA NGAY', '/shop', 3),
('SUMMER BASICS', 'Chất liệu thoáng nhẹ cho ngày dài năng động',
 'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=1920&q=90&fit=crop&crop=center',
 'IMAGE', 'KHÁM PHÁ', '/shop', 4);
