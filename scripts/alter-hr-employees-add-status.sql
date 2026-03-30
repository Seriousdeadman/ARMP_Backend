-- Run if startup migrator did not run (non-Postgres or failure). Fixes 500 on /api/hr/portal/leave-summary
-- when Hibernate expects Employee.status but the column was never added.
ALTER TABLE hr_employees ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';
