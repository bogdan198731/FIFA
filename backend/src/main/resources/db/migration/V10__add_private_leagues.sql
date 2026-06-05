CREATE TABLE private_leagues (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    invite_code VARCHAR(8)   NOT NULL UNIQUE,
    owner_id    BIGINT       NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE league_members (
    id         BIGSERIAL PRIMARY KEY,
    league_id  BIGINT      NOT NULL REFERENCES private_leagues(id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (league_id, user_id)
);

CREATE INDEX idx_league_members_league_id ON league_members(league_id);
CREATE INDEX idx_league_members_user_id   ON league_members(user_id);
