-- Extend option_source column to fit position-specific variants
-- (e.g. PLAYERS_GOALKEEPERS = 19 chars, PLAYERS_MIDFIELDERS = 19 chars)
ALTER TABLE tournament_questions
    ALTER COLUMN option_source TYPE VARCHAR(30);

-- Player roster table — populated by the admin sync-squads endpoint
CREATE TABLE players (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    team_name       VARCHAR(64)  NOT NULL,
    position        VARCHAR(30)  NOT NULL,
    photo           VARCHAR(500),
    api_player_id   BIGINT,
    api_team_id     INT
);

CREATE INDEX ix_players_position  ON players (position);
CREATE UNIQUE INDEX uq_players_api_id ON players (api_player_id)
    WHERE api_player_id IS NOT NULL;
