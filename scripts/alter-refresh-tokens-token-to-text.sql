-- Run once if register/login fail with: value too long for type character varying(255) on refresh_tokens insert.
-- The app also attempts this ALTER on startup when using PostgreSQL (see PostgresRefreshTokenColumnMigrator).

ALTER TABLE refresh_tokens
  ALTER COLUMN token TYPE TEXT USING token::text;
