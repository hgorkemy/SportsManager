package com.sportsmanager.football;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.engine.SegmentResult;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates football matches period by period.
 *
 * TODO (Berke): Implement simulation logic.
 * - Goal probability: attackRating / (attackRating + defenseRating)
 * - Injury probability: 8% per period
 * - 2 periods (halves) per match
 */
public class FootballMatchEngine implements MatchEngine {

    private int currentPeriod = 0;
    private final int totalPeriods = 2;
    private MatchResult currentMatchResult;
    private final List<MatchEvent> allEvents = new ArrayList<>();
    private final List<MatchEvent> lastPeriodEvents = new ArrayList<>();

    @Override
    public SegmentResult simulateNextPeriod(Team home, Team away) {
        // TODO (Berke): Implement period simulation
        currentPeriod++;
        lastPeriodEvents.clear();
        return new SegmentResult(currentPeriod, 0, 0);
    }

    @Override
    public boolean hasNextPeriod() {
        return currentPeriod < totalPeriods;
    }

    @Override
    public MatchResult getFinalResult() {
        return currentMatchResult;
    }

    @Override
    public MatchResult simulateFullMatch(Team home, Team away) {
        // TODO (Berke): Implement full match simulation
        resetMatch();
        currentMatchResult = new MatchResult(home, away, 1, 1);
        while (hasNextPeriod()) simulateNextPeriod(home, away);
        return currentMatchResult;
    }

    @Override public List<MatchEvent> getLastPeriodEvents() { return List.copyOf(lastPeriodEvents); }
    @Override public List<MatchEvent> getAllMatchEvents()    { return List.copyOf(allEvents); }

    @Override
    public void resetMatch() {
        currentPeriod = 0;
        allEvents.clear();
        lastPeriodEvents.clear();
        currentMatchResult = null;
    }
}
