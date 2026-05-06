package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Tactic;
import com.sportsmanager.core.model.Team;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

// Tactics selection screen
public class TacticsController {

    @FXML private ComboBox<String> tacticComboBox;
    @FXML private Label attackLabel;
    @FXML private Label defenseLabel;
    @FXML private Label descLabel;
    @FXML private Label currentTacticLabel;

    private List<Tactic> tactics;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        tactics = session.getSport().getAvailableTactics();

        for (Tactic t : tactics) {
            tacticComboBox.getItems().add(t.getName() + " - " + t.getDescription());
        }

        Team userTeam = session.getUserTeam();
        if (userTeam != null && userTeam.getCurrentTactic() != null) {
            currentTacticLabel.setText("Current: " + userTeam.getCurrentTactic().getName());
        } else {
            currentTacticLabel.setText("Current: None");
        }

        tacticComboBox.setOnAction(e -> updateLabels());

        if (userTeam != null && userTeam.getCurrentTactic() != null) {
            String currentName = userTeam.getCurrentTactic().getName();
            for (int i = 0; i < tactics.size(); i++) {
                if (tactics.get(i).getName().equals(currentName)) {
                    tacticComboBox.getSelectionModel().select(i);
                    updateLabels();
                    break;
                }
            }
        }
    }

    private void updateLabels() {
        int index = tacticComboBox.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        Tactic selected = tactics.get(index);
        attackLabel.setText(String.valueOf(selected.getAttackMultiplier()));
        defenseLabel.setText(String.valueOf(selected.getDefenseMultiplier()));
        descLabel.setText(selected.getDescription());
    }

    @FXML
    private void onApplyTactic() {
        int index = tacticComboBox.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        Tactic selected = tactics.get(index);
        Team userTeam = GameSession.getInstance().getUserTeam();
        if (userTeam != null) {
            userTeam.setCurrentTactic(selected);
            currentTacticLabel.setText("Current: " + selected.getName());
        }
    }

    @FXML
    private void onBack() {
        com.sportsmanager.core.engine.MatchEngine engine = GameSession.getInstance().getMatchEngine();
        if (engine != null && engine.getFinalResult() != null && engine.hasNextPeriod()) {
            SportsManagerApp.navigateTo("MatchView");
        } else {
            SportsManagerApp.navigateTo("DashboardView");
        }
    }
}