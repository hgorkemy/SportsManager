package com.sportsmanager.core.engine;

import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;

import java.util.List;


public interface MatchEngine {
    SegmentResult simulateNextPeriod(Team home, Team away);
    boolean hasNextPeriod();
    MatchResult getFinalResult();
    MatchResult simulateFullMatch(Team home, Team away);
    List<MatchEvent> getLastPeriodEvents();
    List<MatchEvent> getAllMatchEvents();
    void resetMatch();
}
