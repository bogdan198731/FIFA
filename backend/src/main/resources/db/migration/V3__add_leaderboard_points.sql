-- Phase 4: the leaderboard ranks users by accumulated points. The scoring
-- engine in a later phase will write here; for now the column just gives the
-- API something to return.

ALTER TABLE users
    ADD COLUMN total_points BIGINT NOT NULL DEFAULT 0;

-- Demo numbers so the leaderboard renders before any predictions are scored.
UPDATE users SET total_points = 80 WHERE username = 'admin';
UPDATE users SET total_points = 42 WHERE username = 'alice';
UPDATE users SET total_points = 35 WHERE username = 'bob';

CREATE INDEX ix_users_total_points ON users (total_points DESC);
