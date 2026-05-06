package com.sportsmanager.football;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.engine.SegmentResult;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;

public class FootballMatchEngine implements MatchEngine {

    private static final double HOME_ADVANTAGE = 1.1;
    private static final double INJURY_CHANCE = 0.08;
    private static final double YELLOW_CARD_CHANCE = 0.15;

    private static final int PERIOD_MINUTES = 45;
    private static final int TOTAL_PERIODS = 2;


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

        // ratings from lineups
        double homeAttack = home.calculateAttackRating() * HOME_ADVANTAGE;
        double awayAttack = away.calculateAttackRating();
        double homeDefense = home.calculateDefenseRating();
        double awayDefense = away.calculateDefenseRating();

        // apply tactic multipliers if set
        if (home.getCurrentTactic() instanceof FootballTactic ftactic) {
            homeAttack *= ftactic.getAttackMultiplier();
            homeDefense *= ftactic.getDefenseMultiplier();
        }
        if (away.getCurrentTactic() instanceof FootballTactic ftactic) {
            awayAttack *= ftactic.getAttackMultiplier();
            awayDefense *= ftactic.getDefenseMultiplier();
        }

        // minutes 1-45 for first 46-90 for second
        int startMin = (currentPeriod - 1) * PERIOD_MINUTES + 1;

        // simulate goals
        int homeGoalsThisHalf = simulateGoals(homeAttack, awayDefense);
        int awayGoalsThisHalf = simulateGoals(awayAttack, homeDefense);
        
        //register goals as events
        registerGoals(home, homeGoalsThisHalf, startMin, true);
        registerGoals(away, awayGoalsThisHalf, startMin, false);

        // match events
        maybeAddInjury(home, startMin);
        maybeAddInjury(away, startMin);

        maybeAddYellowCard(home, startMin);
        maybeAddYellowCard(away, startMin);

        lastPeriodEvents.sort(Comparator.comparingInt(MatchEvent::getMinute));
        allEvents.addAll(lastPeriodEvents);

        // half-time / full-time marker
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

        if (currentPeriod == TOTAL_PERIODS) { //record appearances of each player
            for (Player p : home.getLineup()) p.recordAppearance();
            for (Player p : away.getLineup()) p.recordAppearance();
        }

        return new SegmentResult(currentPeriod, homeGoalsThisHalf, awayGoalsThisHalf);
    }

    // goal chance based on attack vs defense ratio
    private int simulateGoals(double attack, double defense) {
        double total = attack + defense;
        if (total == 0)
            return 0;

        double goalProb = attack / total;

        int goals = 0;
        for (int i = 0; i < 5; i++) {
            if (random.nextDouble() < goalProb * 0.35) {
                goals++;
            }
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

    // 8% chance of injury per half for a random player
    private void maybeAddInjury(Team team, int startMin) {
        if (random.nextDouble() < INJURY_CHANCE) {
            Player victim = getRandomPlayer(team);
            if (victim != null) {
                int games = 1 + random.nextInt(4); // 1-4 game injury
                victim.injure(games);
                int minute = startMin + random.nextInt(PERIOD_MINUTES);
                MatchEvent injury = new MatchEvent.Builder(MatchEvent.EventType.INJURY, minute)
                        .team(team)
                        .player(victim)
                        .description(victim.getFullName() + " is injured (" + games + " games)")
                        .build();
                lastPeriodEvents.add(injury);
            }
        }
    }

    // 15% chance of yellow card per half
    private void maybeAddYellowCard(Team team, int startMin) {
        if (random.nextDouble() < YELLOW_CARD_CHANCE) {
            Player carded = getRandomPlayer(team);
            if (carded != null) {
                carded.recordYellowCard();
                int minute = startMin + random.nextInt(PERIOD_MINUTES);
                MatchEvent card = new MatchEvent.Builder(MatchEvent.EventType.YELLOW_CARD, minute)
                        .team(team)
                        .player(carded)
                        .description(carded.getFullName() + " gets a yellow card")
                        .build();
                lastPeriodEvents.add(card);
            }
        }
    }

    // pick random player
    private Player getRandomPlayer(Team team) {
        List<Player> pool = new ArrayList<>();
        if (team.getLineup().isEmpty()) {
            pool.addAll(team.getSquad());
        } else {
            pool.addAll(team.getLineup());
        }

        if (pool.isEmpty())
            return null;

        return pool.get(random.nextInt(pool.size()));
    }

    // pick goal scorer (not goalkeeper)
    private Player getRandomScorer(Team team) { 
        List<Player> pool = new ArrayList<>();
        if (team.getLineup().isEmpty()) {
            pool.addAll(team.getSquad());
        } else {
            pool.addAll(team.getLineup());
        }

        if (pool.isEmpty())
            return null;

        // Remove goalkeepers
        pool.removeIf(p -> (p.getPosition() == FootballPosition.GOALKEEPER || p.isInjured()));
        

        // if only goalkeeper
        if (pool.isEmpty())
            return getRandomPlayer(team);

        return pool.get(random.nextInt(pool.size()));
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
