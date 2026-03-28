package com.sportsmanager.core.engine;

/**
 * Holds the result of one match period (half/quarter).
 *
 * TODO (Egemen): Implement this class.
 */
public class SegmentResult {

    private final int periodNumber;
    private final int homeScore;
    private final int awayScore;

    public SegmentResult(int periodNumber, int homeScore, int awayScore) {
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
