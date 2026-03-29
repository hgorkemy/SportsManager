package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.factory.SportRegistry;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

/**
 * Shows all teams, lets user pick one, then navigates to Dashboard.
 */
public class TeamSelectionController {

    @FXML private ListView<Team> teamListView;
    @FXML private Button selectTeamButton;

    private SportFactory sportFactory;
    private List<Team> availableTeams;

    @FXML
    public void initialize() {
        String selectedSportName = GameSession.getInstance().getSelectedSportName();
        sportFactory = SportRegistry.getFactory(selectedSportName);

        if (sportFactory == null) {
            teamListView.getItems().clear();
            selectTeamButton.setDisable(true);
            return;
        }

        availableTeams = sportFactory.generateTeams(20);

        teamListView.getItems().setAll(availableTeams);
        teamListView.setPlaceholder(new javafx.scene.control.Label("No teams available"));
        teamListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                setText(empty || team == null ? null : team.getName());
            }
        });

        selectTeamButton.setDisable(true);
        teamListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTeam, newTeam) ->
            selectTeamButton.setDisable(newTeam == null)
        );
    }

    @FXML
    private void onSelectTeam() {
        Team selectedTeam = teamListView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            return;
        }

        Sport sport = sportFactory.createSport();
        League league = sportFactory.createLeague(availableTeams);
        MatchEngine matchEngine = sportFactory.createMatchEngine();

        GameSession.getInstance().startNewGame(
                sport,
                league,
                selectedTeam,
                matchEngine,
                selectedTeam.getName() + " Save"
        );

        SportsManagerApp.navigateTo("DashboardView");
    }
}
