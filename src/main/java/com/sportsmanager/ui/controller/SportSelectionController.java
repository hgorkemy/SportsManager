package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
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
        sportComboBox.getItems().addAll("Football", "Headball (Coming Soon)");
        sportComboBox.setValue("Football");
        titleLabel.setText("Sports Manager");
    }

    @FXML
    private void onStartGame() {
        String selected = sportComboBox.getValue();
        if ("Football".equals(selected)) {
            SportsManagerApp.navigateTo("TeamSelectionView");
        }
    }
}
