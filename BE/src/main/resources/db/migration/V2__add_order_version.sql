-- ============================================================
-- Mono Wear - V2: Add version column to orders and fix skus version default
-- ============================================================

-- Add version column to orders for Optimistic Locking
ALTER TABLE orders ADD COLUMN version INT NOT NULL DEFAULT 0;
