ALTER TABLE matches
    ADD COLUMN prediction_lock_override VARCHAR(10);

ALTER TABLE matches
    ADD CONSTRAINT ck_matches_prediction_lock_override
    CHECK (prediction_lock_override IS NULL OR prediction_lock_override IN ('LOCKED', 'UNLOCKED'));
