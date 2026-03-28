-- Run once against PostgreSQL if existing rows still use PENDING (replaced by NEW).
UPDATE hr_candidates SET status = 'NEW' WHERE status = 'PENDING';
