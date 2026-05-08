package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchEvent;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    private static final double ICON_SIZE = 18;
    private static final Image GOAL_ICON         = loadIcon("ball.png");
    private static final Image YELLOW_ICON       = loadIcon("yellow-card.png");
    private static final Image RED_CARD_ICON     = loadIcon("suspension.png");
    private static final Image INJURY_ICON       = loadIcon("band-aid.png");
    private static final Image SUSPENSION_ICON   = loadIcon("suspension.png");
    private static final Image THROW_ICON        = loadIcon("throw.png");
    private static final Image PENALTY_ICON      = loadIcon("throw.png");   // spot kick

    private static Image loadIcon(String name) {
        var url = MatchController.class.getResource("/com/sportsmanager/ui/icons/" + name);
        return url == null ? null : new Image(url.toExternalForm());
    }

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
            statusLabel.setText("No match for you this week — other results simulated.");
            simulateButton.setDisable(true);
            dashboardButton.setVisible(true);
            simulateOtherMatches(matchDay, null);
            GameSession.getInstance().setMatchPlayedThisWeek(true);
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

        final Team userTeamRef = userTeam;
        final Team homeTeamRef = currentFixture.getHome();
        eventLog.getSelectionModel().clearSelection();
        eventLog.setSelectionModel(null);
        eventLog.setCellFactory(lv -> new ListCell<MatchEvent>() {
            @Override
            protected void updateItem(MatchEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #111827; -fx-border-color: transparent;");
                    return;
                }
                setText(null);

                // ── Period-end / full-time separator ─────────────────────────
                if (item.getType() == MatchEvent.EventType.PERIOD_END
                        || item.getType() == MatchEvent.EventType.MATCH_END) {
                    Label marker = new Label(item.getDescription());
                    marker.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px;");
                    HBox markerBox = new HBox(marker);
                    markerBox.setAlignment(Pos.CENTER);
                    markerBox.maxWidthProperty().bind(lv.widthProperty().subtract(40));
                    setGraphic(markerBox);
                    setStyle("-fx-background-color: #0d1520; -fx-border-color: transparent;");
                    return;
                }

                Team t     = item.getTeam();
                boolean isUser = t != null && t == userTeamRef;
                boolean isHome = t != null && t == homeTeamRef;  // home always left

                // ── Row background ────────────────────────────────────────────
                // Slightly lifted panels so near-white text reads cleanly
                if (isUser) {
                    setStyle("-fx-background-color: #1a3555; -fx-border-color: transparent;"); // dark blue  — user
                } else if (t != null) {
                    setStyle("-fx-background-color: #0d2e3d; -fx-border-color: transparent;"); // dark teal  — opponent
                } else {
                    setStyle("-fx-background-color: #111827; -fx-border-color: transparent;");
                }

                // ── Minute badge ──────────────────────────────────────────────
                Label minuteLabel = new Label(item.getMinute() + "'");
                minuteLabel.setMinWidth(50);
                minuteLabel.setAlignment(Pos.CENTER);
                minuteLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");

                // ── Description — near-white; each side has a matching cool tint ──
                Label desc = new Label(item.getDescription());
                desc.setStyle(isUser
                        ? "-fx-text-fill: #e8f4ff; -fx-font-size: 12px;"   // near-white, blue tint  — user
                        : "-fx-text-fill: #e4f6f9; -fx-font-size: 12px;"); // near-white, teal tint  — opponent

                Image icon = iconFor(item);
                if (icon != null) {
                    desc.setGraphic(makeIconView(icon));
                    desc.setGraphicTextGap(8);
                    // Icon faces the centre: home (left side) → icon right, away (right side) → icon left
                    desc.setContentDisplay(isHome ? ContentDisplay.RIGHT : ContentDisplay.LEFT);
                }

                // ── Side: home team always LEFT, away team always RIGHT ───────
                HBox left  = new HBox(8);
                HBox right = new HBox(8);
                if (isHome)          left.getChildren().add(desc);
                else if (t != null)  right.getChildren().add(desc);

                left.setAlignment(Pos.CENTER_RIGHT);
                right.setAlignment(Pos.CENTER_LEFT);
                left.setPrefWidth(0);   left.setMinWidth(0);
                right.setPrefWidth(0);  right.setMinWidth(0);
                HBox.setHgrow(left,  Priority.ALWAYS);
                HBox.setHgrow(right, Priority.ALWAYS);

                HBox row = new HBox(left, minuteLabel, right);
                row.setAlignment(Pos.CENTER);
                row.maxWidthProperty().bind(lv.widthProperty().subtract(40));
                setGraphic(row);
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
            // Only interrupt for injuries — red-card (suspended) players stay off the pitch
            // but the team plays on with fewer men; no forced substitution needed.
            GameSession session = GameSession.getInstance();
            Team userTeam = session.getUserTeam();
            List<Player> injuredInLineup = userTeam.getLineup().stream()
                    .filter(Player::isInjured)
                    .collect(java.util.stream.Collectors.toList());

            if (!injuredInLineup.isEmpty()) {
                String names = injuredInLineup.stream()
                        .map(p -> p.getFirstName() + " " + p.getLastName())
                        .collect(java.util.stream.Collectors.joining(", "));
                String msg = "⚠ Injured: " + names + " — please substitute before the 2nd half.";
                session.setPendingInjuryMessage(msg);
                session.setTacticsContext(GameSession.TacticsContext.MID_MATCH);
                SportsManagerApp.navigateTo("TacticsLineupView");
                return;
            }

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

            // Simulate remaining fixtures for this week
            simulateOtherMatches(league.getCurrentMatchDay(), currentFixture);

            // Mark match as played — user must click "Next Week" on Dashboard to advance
            GameSession.getInstance().setMatchPlayedThisWeek(true);
        }
    }

    @FXML
    private void onChangeTactics() {
        GameSession session = GameSession.getInstance();
        session.setTacticsContext(GameSession.TacticsContext.MID_MATCH);
        SportsManagerApp.navigateTo(session.getSport().getTacticsViewName());
    }

    @FXML
    private void onReturnDashboard() {
        if (league.isSeasonOver()) {
            SportsManagerApp.navigateTo("EndOfSeasonView");
        } else {
            SportsManagerApp.navigateTo("DashboardView");
        }
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

    private ImageView makeIconView(Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(ICON_SIZE);
        iv.setFitHeight(ICON_SIZE);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private Image iconFor(MatchEvent e) {
        return switch (e.getType()) {
            case GOAL              -> GOAL_ICON;
            case YELLOW_CARD       -> YELLOW_ICON;
            case RED_CARD          -> RED_CARD_ICON;
            case INJURY            -> INJURY_ICON;
            case SUSPENSION        -> SUSPENSION_ICON;
            case SEVEN_METRE_THROW -> THROW_ICON;
            case PENALTY           -> PENALTY_ICON;
            default                -> null;
        };
    }

    // Ensure team has a valid starting lineup (works for any sport)
    private void autoSetLineup(Team team) {
        boolean needsReset = team.getLineup().isEmpty()
                || team.getLineup().stream().anyMatch(p -> p.isInjured() || p.isSuspended());
        if (!needsReset) return;

        int needed = GameSession.getInstance().getSport().getPlayersPerTeam();
        // Exclude both injured AND suspended players from the replacement pool
        List<Player> healthy = team.getHealthyPlayers().stream()
                .filter(p -> !p.isSuspended())
                .collect(java.util.stream.Collectors.toList());
        List<Player> gks = new ArrayList<>();
        List<Player> outfield = new ArrayList<>();
        for (Player p : healthy) {
            if (p.getPosition() != null && "GK".equals(p.getPosition().getCode()))
                gks.add(p);
            else
                outfield.add(p);
        }

        if (gks.isEmpty() || outfield.size() < needed - 1) return;

        List<Player> lineup = new ArrayList<>();
        lineup.add(gks.get(0));
        lineup.addAll(outfield.subList(0, needed - 1));

        try { team.setLineup(lineup); } catch (Exception ignored) {}
    }
}
