package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// Match simulation UI
public class MatchController {

    @FXML
    private Label homeTeamLabel;
    @FXML
    private Label awayTeamLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label periodLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button simulateButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private VBox halftimeOverlay;
    @FXML
    private ListView<MatchEvent> eventLog;

    private MatchEngine engine;
    private League league;
    private Fixture currentFixture;
    private ObservableList<MatchEvent> events;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        engine = session.getMatchEngine();
        league = session.getLeague();
        Team userTeam = session.getUserTeam();

        MatchDay matchDay = league.getCurrentMatchDay();
        if (matchDay == null) {
            statusLabel.setText("Season is over. No matches.");
            simulateButton.setDisable(true);
            dashboardButton.setVisible(true);
            return;
        }

        currentFixture = matchDay.getFixtureFor(userTeam);
        if (currentFixture == null) {
            statusLabel.setText("No match for you this week.");
            simulateButton.setDisable(true);
            dashboardButton.setVisible(true);
            simulateOtherMatches(matchDay, null);
            league.advanceWeek();
            return;
        }

        homeTeamLabel.setText(currentFixture.getHome().getName());
        awayTeamLabel.setText(currentFixture.getAway().getName());

        if (engine.getFinalResult() == null) {
            scoreLabel.setText("0 - 0");
            periodLabel.setText("Pre-Match");
            statusLabel.setText("Ready to start.");
            simulateButton.setText("Start 1st Half");
            halftimeOverlay.setVisible(false);
            dashboardButton.setVisible(false);
            simulateButton.setVisible(true);
        } else {
            scoreLabel.setText(engine.getFinalResult().getHomeScore() + " - " + engine.getFinalResult().getAwayScore());
            if (engine.hasNextPeriod()) {
                periodLabel.setText("Half-Time");
                statusLabel.setText("Both teams resting...");
                simulateButton.setText("Start 2nd Half");
                halftimeOverlay.setVisible(true);
                dashboardButton.setVisible(false);
                simulateButton.setVisible(true);
            } else {
                periodLabel.setText("Full-Time");
                statusLabel.setText("Match Complete!");
                simulateButton.setVisible(false);
                halftimeOverlay.setVisible(false);
                dashboardButton.setVisible(true);
            }
        }

        events = FXCollections.observableArrayList();
        if (engine.getFinalResult() != null) {
            events.addAll(engine.getAllMatchEvents());
        }
        eventLog.setItems(events);
        if (!events.isEmpty()) {
            eventLog.scrollTo(events.size() - 1);
        }

        eventLog.setCellFactory(lv -> new ListCell<MatchEvent>() {
            @Override
            protected void updateItem(MatchEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMinute() + "' - " + item.getDescription());
                }
            }
        });

        // Ensure lineups are valid
        autoSetLineup(currentFixture.getHome());
        autoSetLineup(currentFixture.getAway());
    }

    @FXML
    private void onSimulatePeriod() {
        if (!engine.hasNextPeriod())
            return;

        Team home = currentFixture.getHome();
        Team away = currentFixture.getAway();

        engine.simulateNextPeriod(home, away);

        // Update list and scroll to bottom
        events.clear();
        events.addAll(engine.getAllMatchEvents());
        if (!events.isEmpty())
            eventLog.scrollTo(events.size() - 1);

        // Update Score
        scoreLabel.setText(engine.getFinalResult().getHomeScore() + " - " + engine.getFinalResult().getAwayScore());

        if (engine.hasNextPeriod()) {
            periodLabel.setText("Half-Time");
            statusLabel.setText("Both teams resting...");
            simulateButton.setText("Start 2nd Half");
            halftimeOverlay.setVisible(true);
        } else {
            periodLabel.setText("Full-Time");
            statusLabel.setText("Match Complete!");
            simulateButton.setVisible(false);
            halftimeOverlay.setVisible(false);
            dashboardButton.setVisible(true);

            // Record match result
            currentFixture.setResult(engine.getFinalResult());
            league.recordResult(currentFixture.getResult());

            // simulation for the remaining week games
            simulateOtherMatches(league.getCurrentMatchDay(), currentFixture);
            league.advanceWeek();
        }
    }

    @FXML
    private void onChangeTactics() {
        SportsManagerApp.navigateTo("TacticsView");
    }

    @FXML
    private void onReturnDashboard() {
        SportsManagerApp.navigateTo("DashboardView");
    }

    // Runs other matches behind the scenes
    private void simulateOtherMatches(MatchDay matchDay, Fixture userFixture) {
        for (Fixture f : matchDay.getFixtures()) {
            if (f == userFixture)
                continue;
            autoSetLineup(f.getHome());
            autoSetLineup(f.getAway());
            f.setResult(engine.simulateFullMatch(f.getHome(), f.getAway()));
            league.recordResult(f.getResult());
        }
    }

    // Ensure other opponents have a valid 11
    private void autoSetLineup(Team team) {
        if (!team.getLineup().isEmpty())
            return;

        List<Player> healthy = team.getHealthyPlayers();
        List<Player> gks = new ArrayList<>();
        List<Player> outfield = new ArrayList<>();
        for (Player p : healthy) {
            if (p.getPosition() == FootballPosition.GOALKEEPER) {
                gks.add(p);
            } else {
                outfield.add(p);
            }
        }
        if (gks.isEmpty() || outfield.size() < 10)
            return;

        List<Player> eleven = new ArrayList<>();
        eleven.add(gks.get(0));
        eleven.addAll(outfield.subList(0, 10));

        team.setLineup(eleven);
    }
}
