package com.sportsmanager;

import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.football.FootballSport;
import com.sportsmanager.football.FootballTactic;
import com.sportsmanager.football.FootballTeam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Egemen's shared interfaces and match model classes.
 */
class MatchResultTest {

    @Test
    void footballSportUsesElevenPlayersPerTeam() {
        FootballSport sport = new FootballSport();

        assertEquals(11, sport.getPlayersPerTeam());
    }

    @Test
    void matchResultStoresScoreCorrectly() {
        FootballTeam home = new FootballTeam("Galatasaray", null);
        FootballTeam away = new FootballTeam("Fenerbahce", null);
        MatchResult result = new MatchResult(home, away, 1, 1);

        result.addHomeGoal();
        result.addHomeGoal();
        result.addAwayGoal();

        assertEquals(2, result.getHomeScore());
        assertEquals(1, result.getAwayScore());
    }

    @Test
    void matchEventBuilderCreatesGoalEvent() {
        FootballTeam team = new FootballTeam("Besiktas", null);
        MatchEvent event = new MatchEvent.Builder(MatchEvent.EventType.GOAL, 34)
                .team(team)
                .description("Clinical finish from the edge of the box")
                .build();

        assertEquals(MatchEvent.EventType.GOAL, event.getType());
        assertEquals(34, event.getMinute());
        assertEquals(team, event.getTeam());
        assertEquals("Clinical finish from the edge of the box", event.getDescription());
    }

    @Test
    void footballTacticReturnsConfiguredAttackAndDefenseMultipliers() {
        FootballTactic tactic = FootballTactic.offensive();

        assertEquals(1.2, tactic.getAttackMultiplier());
        assertEquals(0.9, tactic.getDefenseMultiplier());
    }
}
