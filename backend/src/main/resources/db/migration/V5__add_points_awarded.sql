ALTER TABLE predictions
    ADD COLUMN points_awarded INTEGER NOT NULL DEFAULT 0;

ALTER TABLE tournament_answers
    ADD COLUMN points_awarded INTEGER NOT NULL DEFAULT 0;

CREATE INDEX ix_predictions_points_awarded ON predictions (points_awarded);
CREATE INDEX ix_tournament_answers_points_awarded ON tournament_answers (points_awarded);
