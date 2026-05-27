-- Phase 2: core schema for users, matches, predictions, and tournament-wide
-- side-bet questions.

CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(254) NOT NULL,
    password    VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role     CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE matches (
    id              BIGSERIAL   PRIMARY KEY,
    home_team       VARCHAR(64) NOT NULL,
    away_team       VARCHAR(64) NOT NULL,
    kickoff_at      TIMESTAMPTZ NOT NULL,
    venue           VARCHAR(128),
    match_type      VARCHAR(20) NOT NULL,
    stage           VARCHAR(20) NOT NULL,
    home_score      INTEGER,
    away_score      INTEGER,
    knockout_winner VARCHAR(64),
    finished        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_matches_type  CHECK (match_type IN ('REGULAR', 'KNOCKOUT')),
    CONSTRAINT ck_matches_stage CHECK (stage IN (
        'GROUP', 'ROUND_OF_16', 'QUARTER_FINAL',
        'SEMI_FINAL', 'THIRD_PLACE', 'FINAL'
    ))
);

CREATE INDEX ix_matches_kickoff_at ON matches (kickoff_at);
CREATE INDEX ix_matches_stage      ON matches (stage);

CREATE TABLE predictions (
    id                    BIGSERIAL   PRIMARY KEY,
    user_id               BIGINT      NOT NULL,
    match_id              BIGINT      NOT NULL,
    predicted_home_score  INTEGER     NOT NULL,
    predicted_away_score  INTEGER     NOT NULL,
    predicted_winner      VARCHAR(64),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_predictions_user
        FOREIGN KEY (user_id)  REFERENCES users   (id) ON DELETE CASCADE,
    CONSTRAINT fk_predictions_match
        FOREIGN KEY (match_id) REFERENCES matches (id) ON DELETE CASCADE,
    CONSTRAINT uk_predictions_user_match UNIQUE (user_id, match_id)
);

CREATE INDEX ix_predictions_user  ON predictions (user_id);
CREATE INDEX ix_predictions_match ON predictions (match_id);

CREATE TABLE tournament_questions (
    id              BIGSERIAL    PRIMARY KEY,
    text            VARCHAR(500) NOT NULL,
    deadline        TIMESTAMPTZ  NOT NULL,
    correct_answer  VARCHAR(200),
    points          INTEGER      NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE tournament_answers (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    question_id  BIGINT       NOT NULL,
    answer       VARCHAR(200) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tournament_answers_user
        FOREIGN KEY (user_id)     REFERENCES users                (id) ON DELETE CASCADE,
    CONSTRAINT fk_tournament_answers_question
        FOREIGN KEY (question_id) REFERENCES tournament_questions (id) ON DELETE CASCADE,
    CONSTRAINT uk_tournament_answers_user_question UNIQUE (user_id, question_id)
);

CREATE INDEX ix_tournament_answers_user     ON tournament_answers (user_id);
CREATE INDEX ix_tournament_answers_question ON tournament_answers (question_id);
