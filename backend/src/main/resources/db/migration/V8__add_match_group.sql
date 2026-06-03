-- Group label for group-stage matches (e.g. "A", "B", … for WC 2026's 12
-- groups). NULL for knockout matches. ("group" is a reserved SQL word, so the
-- column is named group_name.)

ALTER TABLE matches ADD COLUMN group_name VARCHAR(16);

CREATE INDEX ix_matches_group_name ON matches (group_name);
