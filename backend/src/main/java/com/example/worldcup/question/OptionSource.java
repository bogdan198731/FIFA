package com.example.worldcup.question;

public enum OptionSource {
    /** Options stored as a static JSON array in the options column. */
    STATIC,
    /** Options resolved at runtime from all distinct team names in the matches table. */
    TEAMS,
    /** Options resolved at runtime from all player names (future). */
    PLAYERS
}
