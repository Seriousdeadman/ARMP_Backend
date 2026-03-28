-- Run if Hibernate ddl-auto=update did not widen columns (e.g. existing DB from older schema).
-- Fixes: refresh token JWTs exceed varchar(255); CV absolute paths can exceed 255 on long base dirs.

ALTER TABLE refresh_tokens
  ALTER COLUMN token TYPE TEXT;

ALTER TABLE hr_cvs
  ALTER COLUMN file_storage_path TYPE TEXT;
