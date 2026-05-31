-- Add structured options to tournament questions.
-- Stored as a JSON array string; null means free-text answer.
ALTER TABLE tournament_questions ADD COLUMN options TEXT;

-- Fix 6: the 48-team list is defined once in the CTE and reused for Q1 and Q6,
-- so a future team-name correction only needs to happen in one place.
-- Team names match exactly what API-Football (and worldcup.db) uses.
WITH all_teams(opts) AS (
    VALUES (
        '["Algeria","Argentina","Australia","Austria","Belgium","Bosnia & Herzegovina",'
        '"Brazil","Canada","Cape Verde Islands","Colombia","Congo DR","Croatia","Curaçao",'
        '"Czech Republic","Ecuador","Egypt","England","France","Germany","Ghana","Haiti",'
        '"Iran","Iraq","Ivory Coast","Japan","Jordan","Mexico","Morocco","Netherlands",'
        '"New Zealand","Norway","Panama","Paraguay","Portugal","Qatar","Saudi Arabia",'
        '"Scotland","Senegal","South Africa","South Korea","Spain","Sweden","Switzerland",'
        '"Tunisia","Türkiye","Uruguay","USA","Uzbekistan"]'::text
    )
)
INSERT INTO tournament_questions (text, deadline, points, options)
SELECT q.text, q.deadline, q.points,
       CASE WHEN q.use_all_teams THEN (SELECT opts FROM all_teams) ELSE q.own_opts END
FROM (VALUES
    ('Which team will lose in a penalty shootout in the Round of 32?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 10, TRUE,  NULL::text),

    ('Which African team will advance furthest in the tournament?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 10, FALSE,
     '["Algeria","Congo DR","Egypt","Ghana","Ivory Coast","Morocco","Senegal","South Africa","Tunisia"]'),

    ('How many total goals will be scored in the group stage?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 8,  FALSE,
     '["Under 100","100 – 109","110 – 119","120 – 129","130 – 139","140 or more"]'),

    ('Who will win the Golden Glove (best goalkeeper)?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 12, FALSE,
     '["Alisson (Brazil)","Emiliano Martínez (Argentina)","Manuel Neuer (Germany)",'
     '"Thibaut Courtois (Belgium)","Mike Maignan (France)","Édouard Mendy (Senegal)",'
     '"Jordan Pickford (England)","David Raya (Spain)","Dominik Livaković (Croatia)",'
     '"Yann Sommer (Switzerland)","Diogo Costa (Portugal)","Yassine Bounou (Morocco)",'
     '"Mark Flekken (Netherlands)","Sergio Rochet (Uruguay)","Matt Turner (USA)"]'),

    ('Will Argentina reach the Final?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 8,  FALSE,
     '["Yes","No"]'),

    ('Which team will score the most goals in a single match?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 10, TRUE,  NULL::text),

    ('Who will win the Young Player of the Tournament award?',
     TIMESTAMPTZ '2026-06-11 16:00:00+00', 12, FALSE,
     '["Lamine Yamal (Spain)","Warren Zaïre-Emery (France)","Endrick (Brazil)",'
     '"Alejandro Garnacho (Argentina)","Gavi (Spain)","Jude Bellingham (England)",'
     '"Florian Wirtz (Germany)","António Silva (Portugal)","Xavi Simons (Netherlands)",'
     '"Kobbie Mainoo (England)","Pedri (Spain)"]')

) AS q(text, deadline, points, use_all_teams, own_opts);
