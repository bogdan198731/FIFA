-- National teams present in the tournament, and their players.
--
-- national_teams: id (PK, indexed) + name.
-- players:        id (PK, indexed), national_team_id (FK), goals scored,
--                 yellow cards, position, and a goalkeeper-only `saves`
--                 column (nullable — only populated for GOALKEEPER rows).

CREATE TABLE national_teams (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    CONSTRAINT uk_national_teams_name UNIQUE (name)
);

CREATE INDEX ix_national_teams_name ON national_teams (name);

CREATE TABLE players (
    id               BIGSERIAL    PRIMARY KEY,
    national_team_id BIGINT       NOT NULL,
    name             VARCHAR(100) NOT NULL,
    position         VARCHAR(20)  NOT NULL,
    goals_scored     INTEGER      NOT NULL DEFAULT 0,
    yellow_cards     INTEGER      NOT NULL DEFAULT 0,
    -- Goalkeeper-only stat. NULL for outfield players.
    saves            INTEGER,
    CONSTRAINT fk_players_national_team
        FOREIGN KEY (national_team_id) REFERENCES national_teams (id) ON DELETE CASCADE,
    CONSTRAINT ck_players_position CHECK (position IN (
        'GOALKEEPER', 'DEFENDER', 'MIDFIELDER', 'FORWARD'
    ))
);

CREATE INDEX ix_players_national_team ON players (national_team_id);
CREATE INDEX ix_players_position      ON players (position);
