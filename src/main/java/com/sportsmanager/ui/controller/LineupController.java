package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.handball.HandballTeam;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private int maxLineup; // 11 for football, 7 for handball

    @FXML
    public void initialize() {
        userTeam = GameSession.getInstance().getUserTeam();
        maxLineup = (userTeam instanceof HandballTeam) ? 7 : 11;

        // Pre-fill from existing lineup, but drop injured players automatically
        lineupItems = FXCollections.observableArrayList(
            userTeam.getLineup().stream()
                    .filter(p -> !p.isInjured())
                    .collect(Collectors.toList())
        );
        squadItems = FXCollections.observableArrayList(userTeam.getSquad());
        squadItems.removeAll(lineupItems);

        squadList.setItems(squadItems);
        lineupList.setItems(lineupItems);

        // Highlight injured players in red in the squad list
        squadList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setStyle("");
                } else if (p.isInjured()) {
                    setText("⚠ " + p.getFullName()
                            + " [" + (p.getPosition() != null ? p.getPosition().getCode() : "?") + "]"
                            + " OVR:" + p.getOverallRating()
                            + "  — INJURED (" + p.getInjuredGamesRemaining() + " games)");
                    setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                } else {
                    setText(p.toString());
                    setStyle("");
                }
            }
        });

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
        if (lineupItems.size() >= maxLineup) {
            showError("Lineup is full (" + maxLineup + " players max).");
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
        lblCount.setText("(" + lineupItems.size() + " / " + maxLineup + ")");
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
