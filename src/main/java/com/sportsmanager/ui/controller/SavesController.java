package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.util.GameSaveManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.Optional;

/**
 * Shows save files for the selected sport and lets the user load or delete them.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class SavesController {

    @FXML private ListView<String> savesList;
    @FXML private Label            lblTitle;
    @FXML private Label            lblStatus;

    private String selectedSport;

    @FXML
    public void initialize() {
        selectedSport = GameSession.getInstance().getSelectedSportName();

        if (selectedSport != null && !selectedSport.isBlank()) {
            lblTitle.setText("Load Game  —  " + selectedSport);
        } else {
            lblTitle.setText("Load Game");
        }

        refreshList();
    }

    private void refreshList() {
        List<String> saves = (selectedSport != null && !selectedSport.isBlank())
                ? GameSaveManager.listSavesForSport(selectedSport)
                : GameSaveManager.listSaves();

        savesList.getItems().setAll(saves);

        if (saves.isEmpty()) {
            setStatus("No " + (selectedSport != null ? selectedSport : "") + " save files found.", false);
        } else {
            lblStatus.setText("");
        }
    }

    @FXML
    private void onLoad() {
        String selected = savesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Please select a save file first.", true);
            return;
        }
        try {
            GameSaveManager.load(selected);
            SportsManagerApp.navigateTo("DashboardView");
        } catch (Exception e) {
            setStatus("Load failed: " + e.getMessage(), true);
        }
    }

    @FXML
    private void onDelete() {
        String selected = savesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Please select a save file to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Save");
        confirm.setHeaderText("Delete \"" + selected + "\"?");
        confirm.setContentText("This cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                GameSaveManager.delete(selected);
                refreshList();
                setStatus("Deleted: " + selected, false);
            } catch (Exception e) {
                setStatus("Delete failed: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void onBack() {
        SportsManagerApp.navigateTo("SportSelectionView");
    }

    private void setStatus(String msg, boolean error) {
        lblStatus.setText(msg);
        lblStatus.setStyle(error
                ? "-fx-text-fill: #f87171; -fx-font-size: 13px;"
                : "-fx-text-fill: #4ade80; -fx-font-size: 13px;");
    }
}
