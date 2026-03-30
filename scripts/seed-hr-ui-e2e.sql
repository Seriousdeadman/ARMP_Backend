-- Full end-to-end HR UI seed for local development.
-- Safe to run multiple times (idempotent inserts).
-- Password for seeded users: 19112002
--
-- Usage from ARMP_Backend:
-- psql -h localhost -p 5432 -U postgres -d university_portal -f scripts/seed-hr-ui-e2e.sql

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- 1) Core auth users (so you can log in with each role)
-- ---------------------------------------------------------------------------
INSERT INTO users (
  id, created_at, email, first_name, last_name, password_hash, phone, department, role, is_active, is_two_factor_enabled, total_login_count
)
SELECT
  'a0000001-0000-4000-8000-000000000001', now(), 'hr.staff@test.com', 'HR', 'Staff',
  crypt('19112002', gen_salt('bf', 10)), '0000000001', 'HR', 'LOGISTICS_STAFF', true, false, 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('hr.staff@test.com'));

INSERT INTO users (
  id, created_at, email, first_name, last_name, password_hash, phone, department, role, is_active, is_two_factor_enabled, total_login_count
)
SELECT
  'a0000002-0000-4000-8000-000000000002', now(), 'super.admin@test.com', 'Super', 'Admin',
  crypt('19112002', gen_salt('bf', 10)), '0000000002', 'IT', 'SUPER_ADMIN', true, false, 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('super.admin@test.com'));

INSERT INTO users (
  id, created_at, email, first_name, last_name, password_hash, phone, department, role, is_active, is_two_factor_enabled, total_login_count
)
SELECT
  'a0000003-0000-4000-8000-000000000003', now(), 'teacher@test.com', 'Teach', 'Er',
  crypt('19112002', gen_salt('bf', 10)), '0000000003', 'CS', 'TEACHER', true, false, 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('teacher@test.com'));

INSERT INTO users (
  id, created_at, email, first_name, last_name, password_hash, phone, department, role, is_active, is_two_factor_enabled, total_login_count
)
SELECT
  'a0000004-0000-4000-8000-000000000004', now(), 'student@test.com', 'Stu', 'Dent',
  crypt('19112002', gen_salt('bf', 10)), '0000000004', 'CS', 'STUDENT', true, false, 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower('student@test.com'));

INSERT INTO user_profiles (id, user_id)
SELECT gen_random_uuid()::text, u.id
FROM users u
WHERE lower(u.email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com'),
  lower('student@test.com')
)
AND NOT EXISTS (SELECT 1 FROM user_profiles p WHERE p.user_id = u.id);

INSERT INTO user_statistics (id, user_id, total_login_count, total_hours_connected, average_session_duration, total_devices_used)
SELECT gen_random_uuid()::text, u.id, 0, 0.0, 0.0, 0
FROM users u
WHERE lower(u.email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com'),
  lower('student@test.com')
)
AND NOT EXISTS (SELECT 1 FROM user_statistics s WHERE s.user_id = u.id);

-- ---------------------------------------------------------------------------
-- 2) HR lookup data (departments + grades)
-- ---------------------------------------------------------------------------
INSERT INTO hr_departments (id, name)
SELECT 'd0000001-0000-4000-8000-000000000001', 'HR Ops'
WHERE NOT EXISTS (SELECT 1 FROM hr_departments WHERE lower(name) = lower('HR Ops'));

INSERT INTO hr_departments (id, name)
SELECT 'd0000002-0000-4000-8000-000000000002', 'Computer Science'
WHERE NOT EXISTS (SELECT 1 FROM hr_departments WHERE lower(name) = lower('Computer Science'));

INSERT INTO hr_grades (id, name, base_salary, hourly_bonus)
SELECT 'g0000001-0000-4000-8000-000000000001', 'ASSISTANT', 8000.00, 25.00
WHERE NOT EXISTS (SELECT 1 FROM hr_grades WHERE name = 'ASSISTANT');

INSERT INTO hr_grades (id, name, base_salary, hourly_bonus)
SELECT 'g0000002-0000-4000-8000-000000000002', 'MAITRE', 12000.00, 40.00
WHERE NOT EXISTS (SELECT 1 FROM hr_grades WHERE name = 'MAITRE');

INSERT INTO hr_grades (id, name, base_salary, hourly_bonus)
SELECT 'g0000003-0000-4000-8000-000000000003', 'PROF', 18000.00, 60.00
WHERE NOT EXISTS (SELECT 1 FROM hr_grades WHERE name = 'PROF');

-- ---------------------------------------------------------------------------
-- 3) Candidates for recruitment + portal application status
-- ---------------------------------------------------------------------------
INSERT INTO hr_candidates (id, name, email, phone, status, department_id)
SELECT
  'c0000001-0000-4000-8000-000000000001',
  'Teach Er',
  'teacher@test.com',
  '0000000003',
  'NEW',
  d.id
FROM hr_departments d
WHERE lower(d.name) = lower('Computer Science')
  AND NOT EXISTS (SELECT 1 FROM hr_candidates c WHERE lower(c.email) = lower('teacher@test.com'));

INSERT INTO hr_candidates (id, name, email, phone, status, department_id)
SELECT
  'c0000002-0000-4000-8000-000000000002',
  'Accepted Candidate',
  'accepted.candidate@test.com',
  '0000000011',
  'ACCEPTED',
  d.id
FROM hr_departments d
WHERE lower(d.name) = lower('HR Ops')
  AND NOT EXISTS (SELECT 1 FROM hr_candidates c WHERE lower(c.email) = lower('accepted.candidate@test.com'));

INSERT INTO hr_candidates (id, name, email, phone, status, department_id)
SELECT
  'c0000003-0000-4000-8000-000000000003',
  'Rejected Candidate',
  'rejected.candidate@test.com',
  '0000000012',
  'REJECTED',
  d.id
FROM hr_departments d
WHERE lower(d.name) = lower('HR Ops')
  AND NOT EXISTS (SELECT 1 FROM hr_candidates c WHERE lower(c.email) = lower('rejected.candidate@test.com'));

INSERT INTO hr_cvs (id, skills_and_experience, candidate_id)
SELECT
  'f0000001-0000-4000-8000-000000000001',
  '5 years teaching Java and Spring Boot. Experience with curriculum design.',
  c.id
FROM hr_candidates c
WHERE lower(c.email) = lower('teacher@test.com')
  AND NOT EXISTS (SELECT 1 FROM hr_cvs cv WHERE cv.candidate_id = c.id);

INSERT INTO hr_cvs (id, skills_and_experience, candidate_id)
SELECT
  'f0000002-0000-4000-8000-000000000002',
  'Operations specialist with HR process automation and analytics background.',
  c.id
FROM hr_candidates c
WHERE lower(c.email) = lower('accepted.candidate@test.com')
  AND NOT EXISTS (SELECT 1 FROM hr_cvs cv WHERE cv.candidate_id = c.id);

INSERT INTO hr_interviews (id, interview_date, location, score, status, candidate_id)
SELECT
  'i0000001-0000-4000-8000-000000000001',
  now() + interval '2 days',
  'Room A1',
  NULL,
  'PLANNED',
  c.id
FROM hr_candidates c
WHERE lower(c.email) = lower('teacher@test.com')
  AND NOT EXISTS (
    SELECT 1
    FROM hr_interviews i
    WHERE i.candidate_id = c.id
      AND i.status = 'PLANNED'
  );

INSERT INTO hr_interviews (id, interview_date, location, score, status, candidate_id)
SELECT
  'i0000002-0000-4000-8000-000000000002',
  now() - interval '3 days',
  'Room B2',
  16,
  'COMPLETED',
  c.id
FROM hr_candidates c
WHERE lower(c.email) = lower('accepted.candidate@test.com')
  AND NOT EXISTS (
    SELECT 1
    FROM hr_interviews i
    WHERE i.candidate_id = c.id
      AND i.status = 'COMPLETED'
  );

INSERT INTO hr_interviews (id, interview_date, location, score, status, candidate_id)
SELECT
  'i0000003-0000-4000-8000-000000000003',
  now() - interval '2 days',
  'Room C3',
  8,
  'COMPLETED',
  c.id
FROM hr_candidates c
WHERE lower(c.email) = lower('rejected.candidate@test.com')
  AND NOT EXISTS (
    SELECT 1
    FROM hr_interviews i
    WHERE i.candidate_id = c.id
      AND i.status = 'COMPLETED'
  );

-- ---------------------------------------------------------------------------
-- 4) Employees for portal leave summary + admin directory
-- ---------------------------------------------------------------------------
INSERT INTO hr_employees (id, name, email, hire_date, leave_balance, grade_id, department_id, source_candidate_id)
SELECT
  'e0000001-0000-4000-8000-000000000001',
  'HR Staff',
  'hr.staff@test.com',
  CURRENT_DATE - 180,
  21,
  g.id,
  d.id,
  NULL
FROM hr_grades g
CROSS JOIN hr_departments d
WHERE g.name = 'ASSISTANT'
  AND lower(d.name) = lower('HR Ops')
  AND NOT EXISTS (SELECT 1 FROM hr_employees e WHERE lower(e.email) = lower('hr.staff@test.com'));

INSERT INTO hr_employees (id, name, email, hire_date, leave_balance, grade_id, department_id, source_candidate_id)
SELECT
  'e0000002-0000-4000-8000-000000000002',
  'Super Admin',
  'super.admin@test.com',
  CURRENT_DATE - 365,
  15,
  g.id,
  d.id,
  NULL
FROM hr_grades g
CROSS JOIN hr_departments d
WHERE g.name = 'MAITRE'
  AND lower(d.name) = lower('HR Ops')
  AND NOT EXISTS (SELECT 1 FROM hr_employees e WHERE lower(e.email) = lower('super.admin@test.com'));

INSERT INTO hr_employees (id, name, email, hire_date, leave_balance, grade_id, department_id, source_candidate_id)
SELECT
  'e0000003-0000-4000-8000-000000000003',
  'Teach Er',
  'teacher@test.com',
  CURRENT_DATE - 90,
  21,
  g.id,
  d.id,
  NULL
FROM hr_grades g
CROSS JOIN hr_departments d
WHERE g.name = 'ASSISTANT'
  AND lower(d.name) = lower('Computer Science')
  AND NOT EXISTS (SELECT 1 FROM hr_employees e WHERE lower(e.email) = lower('teacher@test.com'));

-- Backfill interview interviewer (requires interviewer_id column from JPA ddl-auto or equivalent)
UPDATE hr_interviews i
SET interviewer_id = e.id
FROM hr_employees e
WHERE lower(e.email) = lower('hr.staff@test.com')
  AND i.interviewer_id IS NULL;

-- ---------------------------------------------------------------------------
-- 5) Leave requests for admin actions + status display
-- ---------------------------------------------------------------------------
INSERT INTO hr_leave_requests (id, start_date, end_date, type, status, requested_days, status_message, employee_id)
SELECT
  'l0000001-0000-4000-8000-000000000001',
  CURRENT_DATE + 3,
  CURRENT_DATE + 5,
  'ANNUAL',
  'PENDING',
  3,
  NULL,
  e.id
FROM hr_employees e
WHERE lower(e.email) = lower('hr.staff@test.com')
  AND NOT EXISTS (
    SELECT 1 FROM hr_leave_requests l WHERE l.id = 'l0000001-0000-4000-8000-000000000001'
  );

INSERT INTO hr_leave_requests (id, start_date, end_date, type, status, requested_days, status_message, employee_id)
SELECT
  'l0000002-0000-4000-8000-000000000002',
  CURRENT_DATE - 20,
  CURRENT_DATE - 18,
  'SICK',
  'APPROVED',
  3,
  'Approved automatically by seed script.',
  e.id
FROM hr_employees e
WHERE lower(e.email) = lower('hr.staff@test.com')
  AND NOT EXISTS (
    SELECT 1 FROM hr_leave_requests l WHERE l.id = 'l0000002-0000-4000-8000-000000000002'
  );

INSERT INTO hr_leave_requests (id, start_date, end_date, type, status, requested_days, status_message, employee_id)
SELECT
  'l0000003-0000-4000-8000-000000000003',
  CURRENT_DATE + 10,
  CURRENT_DATE + 11,
  'EXCEPTIONAL',
  'PENDING',
  2,
  NULL,
  e.id
FROM hr_employees e
WHERE lower(e.email) = lower('super.admin@test.com')
  AND NOT EXISTS (
    SELECT 1 FROM hr_leave_requests l WHERE l.id = 'l0000003-0000-4000-8000-000000000003'
  );

INSERT INTO hr_leave_requests (id, start_date, end_date, type, status, requested_days, status_message, employee_id)
SELECT
  'l0000004-0000-4000-8000-000000000004',
  CURRENT_DATE - 40,
  CURRENT_DATE - 39,
  'ANNUAL',
  'REJECTED',
  2,
  'Rejected due to overlap with critical operations.',
  e.id
FROM hr_employees e
WHERE lower(e.email) = lower('super.admin@test.com')
  AND NOT EXISTS (
    SELECT 1 FROM hr_leave_requests l WHERE l.id = 'l0000004-0000-4000-8000-000000000004'
  );

-- Quick verification output
SELECT 'users' AS section, count(*) AS total FROM users WHERE lower(email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com'),
  lower('student@test.com')
)
UNION ALL
SELECT 'departments', count(*) FROM hr_departments WHERE lower(name) IN (lower('HR Ops'), lower('Computer Science'))
UNION ALL
SELECT 'grades', count(*) FROM hr_grades WHERE name IN ('ASSISTANT', 'MAITRE', 'PROF')
UNION ALL
SELECT 'candidates', count(*) FROM hr_candidates WHERE lower(email) IN (
  lower('teacher@test.com'),
  lower('accepted.candidate@test.com'),
  lower('rejected.candidate@test.com')
)
UNION ALL
SELECT 'employees', count(*) FROM hr_employees WHERE lower(email) IN (
  lower('hr.staff@test.com'),
  lower('super.admin@test.com'),
  lower('teacher@test.com')
)
UNION ALL
SELECT 'leave_requests', count(*) FROM hr_leave_requests WHERE id IN (
  'l0000001-0000-4000-8000-000000000001',
  'l0000002-0000-4000-8000-000000000002',
  'l0000003-0000-4000-8000-000000000003',
  'l0000004-0000-4000-8000-000000000004'
);
