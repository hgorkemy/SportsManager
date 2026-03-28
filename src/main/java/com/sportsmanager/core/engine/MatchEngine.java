package com.sportsmanager.core.engine;

import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;

import java.util.List;

/**
 * Match simulation engine interface.
 *
 * TODO (Egemen): This interface is defined. No changes needed here.
 */
public interface MatchEngine {
    SegmentResult simulateNextPeriod(Team home, Team away);
    boolean hasNextPeriod();
    MatchResult getFinalResult();
    MatchResult simulateFullMatch(Team home, Team away);
    List<MatchEvent> getLastPeriodEvents();
    List<MatchEvent> getAllMatchEvents();
    void resetMatch();
}
