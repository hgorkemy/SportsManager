package com.sportsmanager;

import com.sportsmanager.core.engine.SegmentResult;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.football.FootballMatchEngine;
import com.sportsmanager.football.FootballTeam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//5 tests for FootballMatchEngine Implemented by Berke

class MatchEngineTest {

    private FootballMatchEngine engine;
    private FootballTeam homeTeam;
    private FootballTeam awayTeam;

    @BeforeEach
    void setUp() {
        engine = new FootballMatchEngine();
        homeTeam = new FootballTeam("Galatasaray", "gs.png");
        awayTeam = new FootballTeam("Fenerbahce", "fb.png");
    }

    @Test
    void hasNextPeriod_returnsTrueBeforeAnyPeriodSimulated() {
        assertTrue(engine.hasNextPeriod(), "Engine should have next period initially");
    }

    @Test
    void hasNextPeriod_returnsFalseAfterBothPeriodsSimulated() {
        engine.simulateNextPeriod(homeTeam, awayTeam);
        engine.simulateNextPeriod(homeTeam, awayTeam);
        assertFalse(engine.hasNextPeriod(), "Engine should not have next period after 2 halves");
    }

    @Test
    void simulateNextPeriod_returnsValidSegmentResult() {
        SegmentResult result = engine.simulateNextPeriod(homeTeam, awayTeam);
        assertNotNull(result, "Segment result must not be null");
        assertEquals(1, result.getPeriodNumber(), "First period should be period 1");
        assertTrue(result.getHomeScore() >= 0 && result.getAwayScore() >= 0, "Scores should be non-negative");
    }

    @Test
    void simulateNextPeriod_generatesExpectedMatchEvents() {
        engine.simulateFullMatch(homeTeam, awayTeam);

        List<MatchEvent> events = engine.getAllMatchEvents();
        assertNotNull(events, "Event list should not be null");

        // Ensure GOAL events matches the final score
        MatchResult result = engine.getFinalResult();
        int totalGoals = result.getHomeScore() + result.getAwayScore();

        long goalEventsCount = events.stream()
                .filter(e -> e.getType() == MatchEvent.EventType.GOAL)
                .count();

        assertEquals(totalGoals, goalEventsCount, "Number of recorded GOAL events should match the total score");
    }

    @Test
    void simulateFullMatch_returnsValidMatchResult() {
        MatchResult result = engine.simulateFullMatch(homeTeam, awayTeam);

        assertNotNull(result, "MatchResult must not be null");
        assertEquals(homeTeam, result.getHomeTeam(), "Home team must match");
        assertEquals(awayTeam, result.getAwayTeam(), "Away team must match");
        assertEquals(1, result.getSeason(), "Default season should be 1");
        assertEquals(1, result.getWeek(), "Default week should be 1");
        assertFalse(engine.hasNextPeriod(), "Match should be fully completed after simulateFullMatch");
    }
}
