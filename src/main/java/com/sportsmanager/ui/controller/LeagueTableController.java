package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.StandingRow;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Displays the league standings table.
 * Columns: Pos, Team, P, W, D, L, GF, GA, GD, Pts.
 * The user's team row is highlighted in green.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class LeagueTableController {

    @FXML private TableView<StandingRow>           standingsTable;
    @FXML private TableColumn<StandingRow, Integer> colPos;
    @FXML private TableColumn<StandingRow, String>  colTeam;
    @FXML private TableColumn<StandingRow, Integer> colPlayed;
    @FXML private TableColumn<StandingRow, Integer> colWins;
    @FXML private TableColumn<StandingRow, Integer> colDraws;
    @FXML private TableColumn<StandingRow, Integer> colLosses;
    @FXML private TableColumn<StandingRow, Integer> colGF;
    @FXML private TableColumn<StandingRow, Integer> colGA;
    @FXML private TableColumn<StandingRow, String>  colGD;
    @FXML private TableColumn<StandingRow, Integer> colPts;

    @FXML
    public void initialize() {
        Team userTeam = GameSession.getInstance().getUserTeam();
        List<StandingRow> standings = GameSession.getInstance().getLeague().getStandings();

        standingsTable.setItems(FXCollections.observableArrayList(standings));

        colPos.setCellValueFactory(d ->
                new SimpleObjectProperty<>(standingsTable.getItems().indexOf(d.getValue()) + 1));
        colTeam.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTeam().getName()));
        colPlayed.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getMatchesPlayed()));
        colWins.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getWins()));
        colDraws.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getDraws()));
        colLosses.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getLosses()));
        colGF.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getGoalsFor()));
        colGA.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getGoalsAgainst()));
        colGD.setCellValueFactory(d -> {
            int gd = d.getValue().getGoalDifference();
            return new SimpleStringProperty(gd > 0 ? "+" + gd : String.valueOf(gd));
        });
        colPts.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getPoints()));

        // Highlight user team row in dark green
        standingsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
                protected void updateItem(StandingRow row, boolean empty) {
                        super.updateItem(row, empty);
                        setStyle(""); // clear any direct inline style
                        if (!empty && row != null && row.getTeam().equals(userTeam)) {
                                if (!getStyleClass().contains("user-team-row")) {
                                        getStyleClass().add("user-team-row");
                                }
                        } else {
                                getStyleClass().remove("user-team-row");
                        }
                }
        });
    }

    @FXML
    private void onBackToDashboard() {
        SportsManagerApp.navigateTo("DashboardView");
    }
}
