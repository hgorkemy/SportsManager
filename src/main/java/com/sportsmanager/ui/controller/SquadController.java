package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.football.FootballPlayer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Squad management screen.
 * Shows all squad players with position filter and attribute detail panel.
 */
public class SquadController {

    @FXML private TableView<Player> squadTable;
    @FXML private TableColumn<Player, String>  colName;
    @FXML private TableColumn<Player, String>  colPos;
    @FXML private TableColumn<Player, Integer> colOvr;
    @FXML private TableColumn<Player, String>  colStatus;

    @FXML private Label lblPlayerName;
    @FXML private Label lblPlayerPos;
    @FXML private Label lblPlayerAge;
    @FXML private Label lblSpeed;
    @FXML private Label lblShooting;
    @FXML private Label lblPassing;
    @FXML private Label lblBallControl;
    @FXML private Label lblDefending;
    @FXML private Label lblPhysicality;
    @FXML private Label lblInjury;

    private ObservableList<Player> allPlayers;

    @FXML
    public void initialize() {
        List<Player> squad = GameSession.getInstance().getUserTeam().getSquad();
        allPlayers = FXCollections.observableArrayList(squad);

        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colPos.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getPosition() != null ? d.getValue().getPosition().getCode() : "?"));
        colOvr.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getOverallRating()));
        colStatus.setCellValueFactory(d -> {
            Player p = d.getValue();
            return new SimpleStringProperty(
                    p.isInjured() ? "Injured (" + p.getInjuredGamesRemaining() + "g)" : "Fit");
        });

        squadTable.setItems(allPlayers);
        squadTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showDetails(selected));
    }

    // ── Filter handlers ───────────────────────────────────────────────────────

    @FXML private void onFilterAll() { applyFilter("All"); }
    @FXML private void onFilterGK()  { applyFilter("GK"); }
    @FXML private void onFilterDEF() { applyFilter("DEF"); }
    @FXML private void onFilterMID() { applyFilter("MID"); }
    @FXML private void onFilterFWD() { applyFilter("FWD"); }

    private void applyFilter(String code) {
        if ("All".equals(code)) {
            squadTable.setItems(allPlayers);
        } else {
            List<Player> filtered = allPlayers.stream()
                    .filter(p -> p.getPosition() != null && code.equals(p.getPosition().getCode()))
                    .collect(Collectors.toList());
            squadTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    // ── Detail panel ──────────────────────────────────────────────────────────

    private void showDetails(Player player) {
        if (player == null) {
            lblPlayerName.setText("— Select a player —");
            lblPlayerPos.setText("");
            lblPlayerAge.setText("");
            lblSpeed.setText("");
            lblShooting.setText("");
            lblPassing.setText("");
            lblBallControl.setText("");
            lblDefending.setText("");
            lblPhysicality.setText("");
            lblInjury.setText("");
            return;
        }

        lblPlayerName.setText(player.getFullName() + "   OVR: " + player.getOverallRating());
        lblPlayerPos.setText("Position:  " + (player.getPosition() != null ? player.getPosition().getDisplayName() : "?"));
        lblPlayerAge.setText("Age:       " + player.getAge());

        if (player instanceof FootballPlayer fp) {
            lblSpeed.setText("Speed:        " + fp.getSpeed());
            lblShooting.setText("Shooting:     " + fp.getShooting());
            lblPassing.setText("Passing:      " + fp.getPassing());
            lblBallControl.setText("Ball Control: " + fp.getBallControl());
            lblDefending.setText("Defending:    " + fp.getDefending());
            lblPhysicality.setText("Physicality:  " + fp.getPhysicality());
        }

        lblInjury.setText(player.isInjured()
                ? "INJURED — out for " + player.getInjuredGamesRemaining() + " game(s)"
                : "");
    }

    @FXML
    private void onBackToDashboard() {
        SportsManagerApp.navigateTo("DashboardView");
    }
}
