-- Add ROUND_OF_32 to the stage check constraint (WC 2026 has 48 teams → R32 before R16).
-- Also add api_fixture_id for deduplication when syncing from API-Football.

ALTER TABLE matches DROP CONSTRAINT ck_matches_stage;
ALTER TABLE matches ADD CONSTRAINT ck_matches_stage CHECK (stage IN (
    'GROUP', 'ROUND_OF_32', 'ROUND_OF_16', 'QUARTER_FINAL',
    'SEMI_FINAL', 'THIRD_PLACE', 'FINAL'
));

ALTER TABLE matches ADD COLUMN api_fixture_id BIGINT;
CREATE UNIQUE INDEX uq_matches_api_fixture_id ON matches (api_fixture_id)
    WHERE api_fixture_id IS NOT NULL;
