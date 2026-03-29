package com.sportsmanager.core.engine;

/**
 * Holds the result of one match period (half/quarter).
 */
public class SegmentResult {

    private final int periodNumber;
    private final int homeScore;
    private final int awayScore;

    public SegmentResult(int periodNumber, int homeScore, int awayScore) {
        if (periodNumber < 1) {
            throw new IllegalArgumentException("periodNumber must be at least 1");
        }
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("scores cannot be negative");
        }
        this.periodNumber = periodNumber;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public int getPeriodNumber() { return periodNumber; }
    public int getHomeScore()    { return homeScore; }
    public int getAwayScore()    { return awayScore; }

    @Override
    public String toString() {
        return "Period " + periodNumber + ": " + homeScore + " - " + awayScore;
    }
}
