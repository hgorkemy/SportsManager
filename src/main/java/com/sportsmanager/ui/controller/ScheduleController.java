package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballLeague;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all fixtures grouped by week.
 * Completed matches show the score; upcoming matches show both team names.
 * A week ComboBox and "My team only" checkbox allow filtering.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class ScheduleController {

    @FXML private ComboBox<String>  weekSelector;
    @FXML private CheckBox          chkMyTeamOnly;
    @FXML private ListView<String>  fixtureList;

    private List<MatchDay> allMatchDays;
    private Team userTeam;

    @FXML
    public void initialize() {
        userTeam = GameSession.getInstance().getUserTeam();
        League league = GameSession.getInstance().getLeague();

        // Retrieve full schedule (FootballLeague exposes getSchedule())
        if (league instanceof FootballLeague fl) {
            allMatchDays = new ArrayList<>(fl.getSchedule());
        } else {
            allMatchDays = new ArrayList<>();
        }

        // Populate week selector
        List<String> weekOptions = new ArrayList<>();
        weekOptions.add("All Weeks");
        for (MatchDay md : allMatchDays) {
            weekOptions.add("Week " + md.getWeekNumber());
        }
        weekSelector.setItems(FXCollections.observableArrayList(weekOptions));
        weekSelector.getSelectionModel().selectFirst();

        refreshList();
    }

    @FXML private void onWeekSelected()  { refreshList(); }
    @FXML private void onFilterChanged() { refreshList(); }

    private void refreshList() {
        boolean myTeamOnly = chkMyTeamOnly.isSelected();
        String selected = weekSelector.getValue();

        List<String> items = new ArrayList<>();

        for (MatchDay md : allMatchDays) {
            // Week filter
            if (selected != null
                    && !selected.equals("All Weeks")
                    && !selected.equals("Week " + md.getWeekNumber())) {
                continue;
            }

            boolean headerAdded = false;
            for (Fixture f : md.getFixtures()) {
                // My-team filter
                if (myTeamOnly
                        && !f.getHome().equals(userTeam)
                        && !f.getAway().equals(userTeam)) {
                    continue;
                }
                if (!headerAdded) {
                    items.add("── Week " + md.getWeekNumber()
                            + " ─────────────────────────────────────────");
                    headerAdded = true;
                }
                if (f.isPlayed()) {
                    items.add("  " + f.getHome().getName()
                            + "  " + f.getResult().getHomeScore()
                            + " – " + f.getResult().getAwayScore()
                            + "  " + f.getAway().getName());
                } else {
                    items.add("  " + f.getHome().getName()
                            + "  vs  " + f.getAway().getName());
                }
            }
        }

        fixtureList.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void onBackToDashboard() {
        SportsManagerApp.navigateTo("DashboardView");
    }
}
