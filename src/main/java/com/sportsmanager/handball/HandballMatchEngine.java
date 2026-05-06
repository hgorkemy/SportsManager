package com.sportsmanager.handball;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.engine.SegmentResult;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;

public class HandballMatchEngine implements MatchEngine {

    private static final double HOME_ADVANTAGE     = 1.05;
    private static final double INJURY_CHANCE      = 0.06;
    private static final double YELLOW_CARD_CHANCE = 0.20;
    private static final double SUSPENSION_CHANCE  = 0.35;
    private static final double SEVEN_METRE_PER_MIN_CHANCE = 0.06;
    private static final double SEVEN_METRE_GOAL_CHANCE    = 0.65;

    private static final int PERIOD_MINUTES = 30;
    private static final int TOTAL_PERIODS  = 2;

    private int currentPeriod = 0;
    private MatchResult currentMatchResult;
    private final List<MatchEvent> allEvents = new ArrayList<>();
    private final List<MatchEvent> lastPeriodEvents = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public SegmentResult simulateNextPeriod(Team home, Team away) {
        currentPeriod++;
        lastPeriodEvents.clear();

        if (currentMatchResult == null) {
            currentMatchResult = new MatchResult(home, away, 1, 1);
        }

        double homeAttack  = home.calculateAttackRating()  * HOME_ADVANTAGE;
        double awayAttack  = away.calculateAttackRating();
        double homeDefense = home.calculateDefenseRating();
        double awayDefense = away.calculateDefenseRating();

        // Apply tactic multipliers
        if (home.getCurrentTactic() != null) {
            homeAttack  *= home.getCurrentTactic().getAttackMultiplier();
            homeDefense *= home.getCurrentTactic().getDefenseMultiplier();
        }
        if (away.getCurrentTactic() != null) {
            awayAttack  *= away.getCurrentTactic().getAttackMultiplier();
            awayDefense *= away.getCurrentTactic().getDefenseMultiplier();
        }

        // Minutes: 1-30 (first half), 31-60 (second half)
        int startMin = (currentPeriod - 1) * PERIOD_MINUTES + 1;

        // Regular field goals
        int homeRegular = simulateGoals(homeAttack, awayDefense);
        int awayRegular = simulateGoals(awayAttack, homeDefense);
        registerGoals(home, homeRegular, startMin, true);
        registerGoals(away, awayRegular, startMin, false);

        // 7-metre throws: 6% chance per minute, per team
        int home7m = 0, away7m = 0;
        for (int min = startMin; min < startMin + PERIOD_MINUTES; min++) {
            home7m += maybeAdd7mThrow(home, away, min, true);
            away7m += maybeAdd7mThrow(away, home, min, false);
        }

        // Discipline and injury events
        maybeAddYellowCard(home, startMin);
        maybeAddYellowCard(away, startMin);
        maybeAddSuspension(home, startMin);
        maybeAddSuspension(away, startMin);
        maybeAddInjury(home, startMin);
        maybeAddInjury(away, startMin);

        lastPeriodEvents.sort(Comparator.comparingInt(MatchEvent::getMinute));
        allEvents.addAll(lastPeriodEvents);

        MatchEvent endMarker;
        if (currentPeriod < TOTAL_PERIODS) {
            endMarker = new MatchEvent.Builder(MatchEvent.EventType.PERIOD_END, currentPeriod * PERIOD_MINUTES)
                    .description("--- Half-Time ---").build();
        } else {
            endMarker = new MatchEvent.Builder(MatchEvent.EventType.MATCH_END, TOTAL_PERIODS * PERIOD_MINUTES)
                    .description("--- Full-Time ---").build();
        }
        lastPeriodEvents.add(endMarker);
        allEvents.add(endMarker);

        if (currentPeriod == TOTAL_PERIODS) {
            for (Player p : home.getLineup()) p.recordAppearance();
            for (Player p : away.getLineup()) p.recordAppearance();
        }

        return new SegmentResult(currentPeriod, homeRegular + home7m, awayRegular + away7m);
    }

    /**
     * Goal model targeting ~8 goals per team per half (15-20 total combined).
     * Formula: expected = 8.0 + (attack - defense) * 0.06, clamped to [2, 14].
     * Uses 20 Bernoulli trials so the distribution stays natural.
     */
    private int simulateGoals(double attack, double defense) {
        double diff     = attack - defense;
        double expected = Math.max(2.0, Math.min(14.0, 8.0 + diff * 0.06));
        int goals = 0;
        double chancePerOpp = expected / 20.0;
        for (int i = 0; i < 20; i++) {
            if (random.nextDouble() < chancePerOpp) goals++;
        }
        return goals;
    }

    private void registerGoals(Team team, int count, int startMin, boolean isHome) {
        for (int i = 0; i < count; i++) {
            int minute = startMin + random.nextInt(PERIOD_MINUTES);
            Player scorer = getRandomScorer(team);
            if (scorer != null) scorer.recordGoal();
            lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.GOAL, minute)
                    .team(team)
                    .player(scorer)
                    .description(scorer != null
                            ? scorer.getFullName() + " scores for " + team.getName()
                            : team.getName() + " scores")
                    .build());
            if (isHome) currentMatchResult.addHomeGoal();
            else        currentMatchResult.addAwayGoal();
        }
    }

    /**
     * Called once per minute. 6% chance a 7m throw is awarded.
     * 65% converted (GOAL), 35% saved (SEVEN_METRE_SAVED).
     * Returns 1 if a goal is scored, 0 otherwise.
     */
    private int maybeAdd7mThrow(Team attacking, Team defending, int minute, boolean attackingIsHome) {
        if (random.nextDouble() >= SEVEN_METRE_PER_MIN_CHANCE) return 0;

        Player thrower    = getRandomScorer(attacking);
        Player goalkeeper = getGoalkeeper(defending);

        lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.SEVEN_METRE_THROW, minute)
                .team(attacking)
                .player(thrower)
                .description((thrower != null ? thrower.getFullName() : attacking.getName())
                        + " takes a 7-metre throw")
                .build());

        if (random.nextDouble() < SEVEN_METRE_GOAL_CHANCE) {
            if (thrower != null) thrower.recordGoal();
            lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.GOAL, minute)
                    .team(attacking)
                    .player(thrower)
                    .description((thrower != null ? thrower.getFullName() : attacking.getName())
                            + " converts the 7-metre throw!")
                    .build());
            if (attackingIsHome) currentMatchResult.addHomeGoal();
            else                 currentMatchResult.addAwayGoal();
            return 1;
        } else {
            lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.SEVEN_METRE_SAVED, minute)
                    .team(defending)
                    .player(goalkeeper)
                    .description((goalkeeper != null ? goalkeeper.getFullName() : defending.getName())
                            + " saves the 7-metre throw!")
                    .build());
            return 0;
        }
    }

    private void maybeAddInjury(Team team, int startMin) {
        if (random.nextDouble() >= INJURY_CHANCE) return;
        Player victim = getRandomPlayer(team);
        if (victim == null) return;
        int games  = 1 + random.nextInt(3);
        int minute = startMin + random.nextInt(PERIOD_MINUTES);
        victim.injure(games);
        lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.INJURY, minute)
                .team(team)
                .player(victim)
                .description(victim.getFullName() + " is injured (" + games + " games)")
                .build());
    }

    private void maybeAddYellowCard(Team team, int startMin) {
        if (random.nextDouble() >= YELLOW_CARD_CHANCE) return;
        Player carded = getRandomPlayer(team);
        if (carded == null) return;
        carded.recordYellowCard();
        int minute = startMin + random.nextInt(PERIOD_MINUTES);
        lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.YELLOW_CARD, minute)
                .team(team)
                .player(carded)
                .description(carded.getFullName() + " receives a yellow card")
                .build());
    }

    // 2-minute suspension, common in handball 


    //to-do: 3 suspensions lead to RED CARD!


    private void maybeAddSuspension(Team team, int startMin) {
        if (random.nextDouble() >= SUSPENSION_CHANCE) return;
        Player suspended = getRandomPlayer(team);
        if (suspended == null) return;
        int minute = startMin + random.nextInt(PERIOD_MINUTES);
        lastPeriodEvents.add(new MatchEvent.Builder(MatchEvent.EventType.SUSPENSION, minute)
                .team(team)
                .player(suspended)
                .description(suspended.getFullName() + " receives a 2-minute suspension")
                .build());
    }

    private Player getRandomPlayer(Team team) {
        List<Player> pool = new ArrayList<>(
                team.getLineup().isEmpty() ? team.getSquad() : team.getLineup());
        return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
    }

    // Excludes goalkeepers
    private Player getRandomScorer(Team team) {
        List<Player> pool = new ArrayList<>(
                team.getLineup().isEmpty() ? team.getSquad() : team.getLineup());
        pool.removeIf(p -> (p.getPosition() == HandballPosition.GOALKEEPER || p.isInjured()));

        if (pool.isEmpty()) return getRandomPlayer(team);
        return pool.get(random.nextInt(pool.size()));
    }

    private Player getGoalkeeper(Team team) {
        List<Player> pool = new ArrayList<>(
                team.getLineup().isEmpty() ? team.getSquad() : team.getLineup());
        List<Player> keepers = pool.stream()
                .filter(p -> p.getPosition() == HandballPosition.GOALKEEPER)
                .collect(Collectors.toList());
        return keepers.isEmpty() ? getRandomPlayer(team) : keepers.get(random.nextInt(keepers.size()));
    }

    @Override
    public boolean hasNextPeriod() {
        return currentPeriod < TOTAL_PERIODS;
    }

    @Override
    public MatchResult getFinalResult() {
        return currentMatchResult;
    }

    @Override
    public MatchResult simulateFullMatch(Team home, Team away) {
        resetMatch();
        currentMatchResult = new MatchResult(home, away, 1, 1);
        while (hasNextPeriod())
            simulateNextPeriod(home, away);
        return currentMatchResult;
    }

    @Override
    public List<MatchEvent> getLastPeriodEvents() {
        return List.copyOf(lastPeriodEvents);
    }

    @Override
    public List<MatchEvent> getAllMatchEvents() {
        return List.copyOf(allEvents);
    }

    @Override
    public void resetMatch() {
        currentPeriod = 0;
        allEvents.clear();
        lastPeriodEvents.clear();
        currentMatchResult = null;
    }
}
