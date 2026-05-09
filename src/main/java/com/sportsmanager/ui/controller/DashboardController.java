package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.util.TeamLogoHelper;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;
import com.sportsmanager.util.GameSaveManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;

import java.util.List;


public class DashboardController {

    @FXML private ImageView imgClubLogo;
    @FXML private ImageView imgHomeTeamLogo;
    @FXML private ImageView imgAwayTeamLogo;

    @FXML private Label lblSeason;
    @FXML private Label lblSportName;
    @FXML private Label lblTeamName;
    @FXML private Label lblPosition;
    @FXML private Label lblWeek;
    @FXML private Label lblNextMatch;
    @FXML private Label lblMatchDetail;
    @FXML private Label lblPlayed;
    @FXML private Label lblWins;
    @FXML private Label lblDraws;
    @FXML private Label lblLosses;
    @FXML private Label lblPoints;
    @FXML private Button btnPlayMatch;
    @FXML private Button btnNextWeek;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        League league = session.getLeague();
        Team userTeam = session.getUserTeam();

        // Season finished → go to end-of-season screen automatically
        if (league.isSeasonOver()) {
            Platform.runLater(() -> SportsManagerApp.navigateTo("EndOfSeasonView"));
            return;
        }

        lblSeason.setText("Season " + session.getCurrentSeason());
        lblSportName.setText("[" + session.getSelectedSportName() + "]");
        lblTeamName.setText(userTeam.getName());

        // Club logo
        var clubLogo = TeamLogoHelper.load(userTeam, 48);
        if (clubLogo != null) imgClubLogo.setImage(clubLogo);

        // Current week
        MatchDay current = league.getCurrentMatchDay();
        lblWeek.setText(current != null ? "Week " + current.getWeekNumber() : "—");

        // League position
        List<StandingRow> standings = league.getStandings();
        int position = 1;
        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i).getTeam().equals(userTeam)) {
                position = i + 1;
                break;
            }
        }
        lblPosition.setText("League Position: #" + position + " of " + standings.size());

        // Next match info
        if (current != null) {
            Fixture fixture = current.getFixtureFor(userTeam);
            if (fixture != null) {
                lblNextMatch.setText(fixture.getHome().getName() + " vs " + fixture.getAway().getName());
                String venue = fixture.getHome().equals(userTeam) ? "Home" : "Away";
                lblMatchDetail.setText(venue + " — Week " + current.getWeekNumber());

                // Next match logos
                var homeLogo = TeamLogoHelper.load(fixture.getHome(), 32);
                var awayLogo = TeamLogoHelper.load(fixture.getAway(), 32);
                if (homeLogo != null) imgHomeTeamLogo.setImage(homeLogo);
                if (awayLogo != null) imgAwayTeamLogo.setImage(awayLogo);
            } else {
                lblNextMatch.setText("Bye week — no fixture");
                lblMatchDetail.setText("");
            }
        } else {
            lblNextMatch.setText("No upcoming matches");
            lblMatchDetail.setText("Season complete");
        }

        // Season stats
        StandingRow row = standings.stream()
                .filter(r -> r.getTeam().equals(userTeam))
                .findFirst().orElse(null);
        if (row != null) {
            lblPlayed.setText("P: " + row.getMatchesPlayed());
            lblWins.setText("W: " + row.getWins());
            lblDraws.setText("D: " + row.getDraws());
            lblLosses.setText("L: " + row.getLosses());
            lblPoints.setText("Pts: " + row.getPoints());
        }

        // Button locking: Play Match ↔ Next Week are mutually exclusive
        boolean matchPlayed = session.isMatchPlayedThisWeek();
        boolean seasonOver  = league.isSeasonOver();

        btnPlayMatch.setDisable(matchPlayed || seasonOver);
        btnNextWeek.setDisable(!matchPlayed || seasonOver);

        if (seasonOver) {
            lblNextMatch.setText("Season complete!");
            lblMatchDetail.setText("Check the league table for final standings.");
        }
    }

    // ── Button handlers ────────────────────────────────────────────────────────

    @FXML
    private void onAdvanceWeek() {
        GameSession session = GameSession.getInstance();
        if (!session.isMatchPlayedThisWeek() || session.getLeague().isSeasonOver()) return;
        // Go to training selection — TrainingController will advance the week after
        SportsManagerApp.navigateTo("TrainingView");
    }

    @FXML
    private void onPlayMatch() {
        GameSession session = GameSession.getInstance();
        if (session.isMatchPlayedThisWeek()) return; // safety guard
        session.setTacticsContext(GameSession.TacticsContext.PRE_MATCH);
        SportsManagerApp.navigateTo("TacticsLineupView");
    }

    @FXML
    private void onSave() {
        GameSession session = GameSession.getInstance();

        // Pre-fill with the existing save name (or a sensible default)
        String currentName = session.getSaveName();
        if (currentName == null || currentName.isBlank()) {
            currentName = session.getUserTeam().getName() + " Save";
        }

        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Choose a name for your save file");
        dialog.setContentText("Save name:");

        dialog.showAndWait().ifPresent(name -> {
            name = name.strip();
            if (name.isBlank()) return;
            try {
                GameSaveManager.save(name);
                session.setSaveName(name);          // remember it for next quick-save
                showInfo("Game Saved", "Saved as: " + name + ".json");
            } catch (Exception e) {
                showError("Save Failed", e.getMessage());
            }
        });
    }

    @FXML private void onMainMenu() {
        GameSession.getInstance().reset();
        SportsManagerApp.navigateTo("SportSelectionView");
    }

    @FXML private void onSquad()       { SportsManagerApp.navigateTo("SquadView"); }
    @FXML private void onLeagueTable() { SportsManagerApp.navigateTo("LeagueTableView"); }
    @FXML private void onSchedule()    { SportsManagerApp.navigateTo("ScheduleView"); }
    @FXML private void onTactics() {
        GameSession.getInstance().setTacticsContext(GameSession.TacticsContext.BROWSE);
        SportsManagerApp.navigateTo("TacticsLineupView");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
