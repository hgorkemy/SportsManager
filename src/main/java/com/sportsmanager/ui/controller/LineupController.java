package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.ArrayList;

/**
 * Lineup selection screen.
 * Players are moved between the squad list and the lineup list.
 * "Confirm Lineup" calls Team.setLineup() which validates the selection.
 */
public class LineupController {

    @FXML private ListView<Player> squadList;
    @FXML private ListView<Player> lineupList;
    @FXML private Label lblCount;
    @FXML private Label lblStatus;
    @FXML private Button btnConfirm;

    private ObservableList<Player> squadItems;
    private ObservableList<Player> lineupItems;
    private Team userTeam;

    @FXML
    public void initialize() {
        userTeam = GameSession.getInstance().getUserTeam();

        // Pre-fill from existing lineup, but automatically drop injured players
        java.util.List<Player> healthyLineup = userTeam.getLineup().stream()
                .filter(p -> !p.isInjured())
                .collect(java.util.stream.Collectors.toList());

        lineupItems = FXCollections.observableArrayList(healthyLineup);
        squadItems  = FXCollections.observableArrayList(userTeam.getSquad());
        squadItems.removeAll(lineupItems);

        squadList.setItems(squadItems);
        lineupList.setItems(lineupItems);

        updateCount();
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void onAddToLineup() {
        Player p = squadList.getSelectionModel().getSelectedItem();
        if (p == null) return;
        if (p.isInjured()) {
            showError("Cannot add injured player to lineup.");
            return;
        }
        if (lineupItems.size() >= 11) {
            showError("Lineup is already full (11 players).");
            return;
        }
        squadItems.remove(p);
        lineupItems.add(p);
        lblStatus.setText("");
        updateCount();
    }

    @FXML
    private void onRemoveFromLineup() {
        Player p = lineupList.getSelectionModel().getSelectedItem();
        if (p == null) return;
        lineupItems.remove(p);
        squadItems.add(p);
        lblStatus.setText("");
        updateCount();
    }

    @FXML
    private void onClearLineup() {
        squadItems.addAll(lineupItems);
        lineupItems.clear();
        lblStatus.setText("");
        updateCount();
    }

    @FXML
    private void onConfirmLineup() {
        try {
            userTeam.setLineup(new ArrayList<>(lineupItems));
            GameSession.getInstance().getMatchEngine().resetMatch();
            showSuccess("Lineup confirmed!");
            SportsManagerApp.navigateTo("MatchView");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        SportsManagerApp.navigateTo("DashboardView");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateCount() {
        lblCount.setText("(" + lineupItems.size() + " / 11)");
    }

    private void showError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblStatus.setText(msg);
    }

    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblStatus.setText(msg);
    }
}
