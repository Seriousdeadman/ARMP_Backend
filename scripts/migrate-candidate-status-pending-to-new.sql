-- Align hr_candidates.status with Java enum CandidateStatus: NEW, INTERVIEWING, ACCEPTED, REJECTED.
-- Legacy DBs used PENDING + a CHECK that only allowed PENDING/ACCEPTED/REJECTED, which breaks JPA mapping.

ALTER TABLE hr_candidates DROP CONSTRAINT IF EXISTS hr_candidates_status_check;

UPDATE hr_candidates SET status = 'NEW' WHERE status = 'PENDING';

ALTER TABLE hr_candidates ADD CONSTRAINT hr_candidates_status_check
  CHECK (status::text = ANY (ARRAY[
    'NEW'::text,
    'INTERVIEWING'::text,
    'ACCEPTED'::text,
    'REJECTED'::text
  ]));
