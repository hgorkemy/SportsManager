package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Main hub screen. Shows current week, user team info, next match, and league position.
 * Navigation buttons to Squad, League Table, Schedule, and Play Match.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class DashboardController {

    @FXML private Label lblSeason;
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

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        League league = session.getLeague();
        Team userTeam = session.getUserTeam();

        lblSeason.setText("Season " + session.getCurrentSeason());
        lblTeamName.setText(userTeam.getName());

        // Current week
        MatchDay current = league.getCurrentMatchDay();
        if (current != null) {
            lblWeek.setText("Week " + current.getWeekNumber());
        } else {
            lblWeek.setText("Season Over");
        }

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

        // Next match
        if (current != null) {
            Fixture fixture = current.getFixtureFor(userTeam);
            if (fixture != null) {
                lblNextMatch.setText(fixture.getHome().getName() + " vs " + fixture.getAway().getName());
                String venue = fixture.getHome().equals(userTeam) ? "Home" : "Away";
                lblMatchDetail.setText(venue + " — Week " + current.getWeekNumber());
            } else {
                lblNextMatch.setText("No fixture this week");
                lblMatchDetail.setText("");
            }
        } else {
            lblNextMatch.setText("No upcoming matches");
            lblMatchDetail.setText("Season complete");
        }

        // User team stats from standings
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
    }

    @FXML private void onSquad()       { SportsManagerApp.navigateTo("SquadView"); }
    @FXML private void onLeagueTable() { SportsManagerApp.navigateTo("LeagueTableView"); }
    @FXML private void onSchedule()    { SportsManagerApp.navigateTo("ScheduleView"); }
    @FXML private void onTactics()     { SportsManagerApp.navigateTo("TacticsView"); }
    @FXML private void onPlayMatch()   { SportsManagerApp.navigateTo("LineupView"); }
}
