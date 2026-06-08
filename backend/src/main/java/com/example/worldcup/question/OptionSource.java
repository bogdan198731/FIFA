package com.example.worldcup.question;

public enum OptionSource {
    /** Options stored as a static JSON array in the options column. */
    STATIC,
    /** Options resolved at runtime from all distinct team names in the matches table. */
    TEAMS,
    /** Options resolved at runtime from all player names in the players table. */
    PLAYERS,
    /** Options resolved from players whose position is "Goalkeeper". */
    PLAYERS_GOALKEEPERS,
    /** Options resolved from players whose position is "Defender". */
    PLAYERS_DEFENDERS,
    /** Options resolved from players whose position is "Midfielder". */
    PLAYERS_MIDFIELDERS,
    /** Options resolved from players whose position is "Attacker". */
    PLAYERS_ATTACKERS
}
