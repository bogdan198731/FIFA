-- Phase 2 seed data. Passwords below are placeholder bcrypt hashes; they will
-- be replaced by real credentials once the auth phase introduces password
-- hashing. The strings are valid bcrypt format so no constraint is violated,
-- but they do not authenticate against any known plaintext.

INSERT INTO users (username, email, password, role) VALUES
    ('admin', 'admin@example.com',
     '$2a$10$placeholderplaceholderplaceholderplaceholderpla', 'ADMIN'),
    ('alice', 'alice@example.com',
     '$2a$10$placeholderplaceholderplaceholderplaceholderpla', 'USER'),
    ('bob',   'bob@example.com',
     '$2a$10$placeholderplaceholderplaceholderplaceholderpla', 'USER');

-- A handful of fixtures spanning group stage and knockouts so the rest of the
-- app has something to render.
INSERT INTO matches
    (home_team, away_team, kickoff_at, venue, match_type, stage)
VALUES
    ('Brazil',    'Serbia',    TIMESTAMPTZ '2026-06-14 18:00:00+00',
     'Lusail Stadium',          'REGULAR',  'GROUP'),
    ('France',    'Australia', TIMESTAMPTZ '2026-06-15 18:00:00+00',
     'Al Janoub Stadium',       'REGULAR',  'GROUP'),
    ('Argentina', 'Mexico',    TIMESTAMPTZ '2026-06-16 18:00:00+00',
     'Lusail Stadium',          'REGULAR',  'GROUP'),
    ('Spain',     'Germany',   TIMESTAMPTZ '2026-07-04 18:00:00+00',
     'Education City Stadium',  'KNOCKOUT', 'QUARTER_FINAL'),
    ('Brazil',    'France',    TIMESTAMPTZ '2026-07-10 18:00:00+00',
     'Lusail Stadium',          'KNOCKOUT', 'SEMI_FINAL'),
    ('TBD',       'TBD',       TIMESTAMPTZ '2026-07-18 18:00:00+00',
     'Lusail Stadium',          'KNOCKOUT', 'FINAL');

INSERT INTO tournament_questions (text, deadline, points) VALUES
    ('Which team will win the World Cup?',
     TIMESTAMPTZ '2026-06-13 16:00:00+00', 20),
    ('Who will be the tournament top scorer?',
     TIMESTAMPTZ '2026-06-13 16:00:00+00', 15),
    ('Will any group-stage match end in a 0-0 draw?',
     TIMESTAMPTZ '2026-06-13 16:00:00+00', 5);
