-- Phase: live football data sync agent.
--
-- Adds:
--   * matches.external_id            — opaque ID from the upstream football
--                                      data provider (Football-Data.org by
--                                      default). The sync agent only touches
--                                      matches with a non-null external_id.
--   * matches.result_manual_override — flips true the moment an admin edits a
--                                      match result by hand. The sync agent
--                                      respects this flag and stops overwriting
--                                      the admin's input.
--   * teams                          — cache of upstream team metadata; used
--                                      by the sync agent to resolve external
--                                      team IDs into team names.

ALTER TABLE matches
    ADD COLUMN external_id            VARCHAR(64),
    ADD COLUMN result_manual_override BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX ix_matches_external_id
    ON matches (external_id)
    WHERE external_id IS NOT NULL;

CREATE TABLE teams (
    id              BIGSERIAL    PRIMARY KEY,
    external_id     VARCHAR(64)  NOT NULL,
    name            VARCHAR(64)  NOT NULL,
    short_code      VARCHAR(8),
    last_synced_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_teams_external_id UNIQUE (external_id)
);

CREATE INDEX ix_teams_name ON teams (name);
