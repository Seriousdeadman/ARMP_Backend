-- Dev-only: extra accounts for testing roles (password for all: 19112002).
-- Requires PostgreSQL extension pgcrypto (bcrypt hashes compatible with Spring BCryptPasswordEncoder).
-- Run: psql -h localhost -p 5432 -U postgres -d university_portal -f scripts/seed-dev-users.sql

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (
  id,
  created_at,
  email,
  first_name,
  last_name,
  password_hash,
  phone,
  department,
  role,
  is_active,
  is_two_factor_enabled,
  total_login_count
)
SELECT
  'a0000001-0000-4000-8000-000000000001',
  now(),
  'hr.staff@test.com',
  'HR',
  'Staff',
  crypt('19112002', gen_salt('bf', 10)),
  '0000000001',
  'HR',
  'LOGISTICS_STAFF',
  true,
  false,
  0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('hr.staff@test.com'));

INSERT INTO users (
  id,
  created_at,
  email,
  first_name,
  last_name,
  password_hash,
  phone,
  department,
  role,
  is_active,
  is_two_factor_enabled,
  total_login_count
)
SELECT
  'a0000002-0000-4000-8000-000000000002',
  now(),
  'super.admin@test.com',
  'Super',
  'Admin',
  crypt('19112002', gen_salt('bf', 10)),
  '0000000002',
  'IT',
  'SUPER_ADMIN',
  true,
  false,
  0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('super.admin@test.com'));

INSERT INTO users (
  id,
  created_at,
  email,
  first_name,
  last_name,
  password_hash,
  phone,
  department,
  role,
  is_active,
  is_two_factor_enabled,
  total_login_count
)
SELECT
  'a0000003-0000-4000-8000-000000000003',
  now(),
  'teacher@test.com',
  'Teach',
  'Er',
  crypt('19112002', gen_salt('bf', 10)),
  '0000000003',
  'CS',
  'TEACHER',
  true,
  false,
  0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('teacher@test.com'));

INSERT INTO users (
  id,
  created_at,
  email,
  first_name,
  last_name,
  password_hash,
  phone,
  department,
  role,
  is_active,
  is_two_factor_enabled,
  total_login_count
)
SELECT
  'a0000004-0000-4000-8000-000000000004',
  now(),
  'regular.staff@test.com',
  'Regular',
  'Staff',
  crypt('19112002', gen_salt('bf', 10)),
  '0000000004',
  'Admin',
  'REGULAR_STAFF',
  true,
  false,
  0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('regular.staff@test.com'));

INSERT INTO user_profiles (id, user_id)
SELECT gen_random_uuid()::text, u.id
FROM users u
WHERE lower(u.email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com'),
  lower('regular.staff@test.com')
)
AND NOT EXISTS (SELECT 1 FROM user_profiles p WHERE p.user_id = u.id);

INSERT INTO user_statistics (
  id,
  user_id,
  total_login_count,
  total_hours_connected,
  average_session_duration,
  total_devices_used
)
SELECT
  gen_random_uuid()::text,
  u.id,
  0,
  0.0,
  0.0,
  0
FROM users u
WHERE lower(u.email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com'),
  lower('regular.staff@test.com')
)
AND NOT EXISTS (SELECT 1 FROM user_statistics s WHERE s.user_id = u.id);
