package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.StandingRow;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Comparator;
import java.util.List;

/**
 * End-of-Season screen controller.
 * Shows champion, user's final position, top scorer, and season stats.
 */
public class EndOfSeasonController {

    @FXML private Label lblSeason;
    @FXML private Label lblChampion;
    @FXML private Label lblUserPosition;
    @FXML private Label lblTopScorer;
    @FXML private Label lblStats;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        League league = session.getLeague();
        Team userTeam = session.getUserTeam();

        lblSeason.setText("Season " + session.getCurrentSeason() + " — Final Standings");

        List<StandingRow> standings = league.getStandings();

        // Champion
        if (!standings.isEmpty()) {
            lblChampion.setText(standings.get(0).getTeam().getName());
        }

        // User's final position
        int position = 1;
        StandingRow userRow = null;
        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i).getTeam().equals(userTeam)) {
                position = i + 1;
                userRow = standings.get(i);
                break;
            }
        }
        lblUserPosition.setText("#" + position + " of " + standings.size());

        // Season stats
        if (userRow != null) {
            lblStats.setText(
                "W: " + userRow.getWins() +
                "   D: " + userRow.getDraws() +
                "   L: " + userRow.getLosses() +
                "   Pts: " + userRow.getPoints()
            );
        }

        // Top scorer across all teams
        league.getTeams().stream()
            .flatMap(t -> t.getSquad().stream())
            .max(Comparator.comparingInt(Player::getGoals))
            .ifPresentOrElse(
                p -> lblTopScorer.setText(p.getFullName() + " — " + p.getGoals() + " goals"),
                () -> lblTopScorer.setText("N/A")
            );
    }

    @FXML
    private void onNewSeason() {
        GameSession session = GameSession.getInstance();
        session.advanceSeason();
        SportsManagerApp.navigateTo("TeamSelectionView");
    }

    @FXML
    private void onMainMenu() {
        GameSession.getInstance().reset();
        SportsManagerApp.navigateTo("SportSelectionView");
    }
}
