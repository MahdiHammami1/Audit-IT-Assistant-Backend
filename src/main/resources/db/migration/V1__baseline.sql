-- Flyway baseline migration
-- This file marks the baseline for Flyway.
-- The actual schema is managed by Supabase SQL migrations (001_enums.sql ... 015_storage.sql).
-- Flyway is used only for any future Spring-side schema additions.
-- Set baseline-on-migrate=true in application.properties to skip this on existing DBs.
SELECT 1;
