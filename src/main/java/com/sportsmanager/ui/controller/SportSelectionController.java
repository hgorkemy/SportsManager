package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.factory.SportRegistry;
import com.sportsmanager.core.model.GameSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/**
 * Controls the Sport Selection screen.
 * User picks a sport and proceeds to team selection.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class SportSelectionController {

    @FXML private ComboBox<String> sportComboBox;
    @FXML private Button startButton;
    @FXML private Label titleLabel;

    @FXML
    public void initialize() {
        sportComboBox.getItems().setAll(SportRegistry.getRegisteredSports());
        if (!sportComboBox.getItems().isEmpty()) {
            sportComboBox.setValue(sportComboBox.getItems().getFirst());
        }
        titleLabel.setText("Sports Manager");
    }

    @FXML
    private void onStartGame() {
        String selected = sportComboBox.getValue();
        if (selected != null && SportRegistry.getFactory(selected) != null) {
            GameSession.getInstance().setSelectedSportName(selected);
            SportsManagerApp.navigateTo("TeamSelectionView");
        }
    }
}
