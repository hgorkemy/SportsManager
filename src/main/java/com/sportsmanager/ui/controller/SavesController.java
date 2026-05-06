package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.util.GameSaveManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

/**
 * Shows existing save files and lets the user load one.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class SavesController {

    @FXML private ListView<String> savesList;
    @FXML private Label            lblStatus;

    @FXML
    public void initialize() {
        List<String> saves = GameSaveManager.listSaves();
        savesList.getItems().setAll(saves);
        if (saves.isEmpty()) {
            lblStatus.setText("No save files found.");
            lblStatus.setStyle("-fx-text-fill: #94a3b8;");
        }
    }

    @FXML
    private void onLoad() {
        String selected = savesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Please select a save file first.");
            return;
        }
        try {
            GameSaveManager.load(selected);
            SportsManagerApp.navigateTo("DashboardView");
        } catch (Exception e) {
            lblStatus.setText("Load failed: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        SportsManagerApp.navigateTo("SportSelectionView");
    }
}
