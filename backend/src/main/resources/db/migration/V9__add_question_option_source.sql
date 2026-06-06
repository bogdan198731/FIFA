-- Adds option_source to tournament_questions so options can be resolved
-- dynamically from the DB (TEAMS, PLAYERS) instead of a static JSON array.
-- Existing questions default to STATIC (current behaviour unchanged).

ALTER TABLE tournament_questions
    ADD COLUMN option_source VARCHAR(20) NOT NULL DEFAULT 'STATIC';

-- Switch the two "all 48 teams" questions to TEAMS source and clear their
-- hardcoded options (the service resolves them live from the matches table).
UPDATE tournament_questions
   SET option_source = 'TEAMS',
       options       = NULL
 WHERE text IN (
    'Which team will lose in a penalty shootout in the Round of 32?',
    'Which team will score the most goals in a single match?'
 );
