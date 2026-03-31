package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballTactic;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

// Tactics selection screen
public class TacticsController {

    @FXML
    private ComboBox<String> tacticComboBox;
    @FXML
    private Label attackLabel;
    @FXML
    private Label defenseLabel;
    @FXML
    private Label descLabel;
    @FXML
    private Label currentTacticLabel;

    // All tactics in the same order as the combo box
    private final FootballTactic[] tactics = {
            FootballTactic.balanced(),
            FootballTactic.offensive(),
            FootballTactic.defensive(),
            FootballTactic.control()
    };

    @FXML
    public void initialize() {
        // add each tactic to combo box
        for (FootballTactic t : tactics) {
            tacticComboBox.getItems().add(t.getName() + " - " + t.getDescription());
        }

        // show current tactic
        Team userTeam = GameSession.getInstance().getUserTeam();
        if (userTeam != null && userTeam.getCurrentTactic() != null) {
            currentTacticLabel.setText("Current: " + userTeam.getCurrentTactic().getName());
        } else {
            currentTacticLabel.setText("Current: None");
        }

        // update labels when selection changes
        tacticComboBox.setOnAction(e -> updateLabels());

        // pre-select current tactic if possible
        if (userTeam != null && userTeam.getCurrentTactic() != null) {
            String currentName = userTeam.getCurrentTactic().getName();
            for (int i = 0; i < tactics.length; i++) {
                if (tactics[i].getName().equals(currentName)) {
                    tacticComboBox.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    // update multiplier labels for selected tactic
    private void updateLabels() {
        int index = tacticComboBox.getSelectionModel().getSelectedIndex();
        if (index < 0)
            return;

        FootballTactic selected = tactics[index];
        attackLabel.setText(String.valueOf(selected.getAttackMultiplier()));
        defenseLabel.setText(String.valueOf(selected.getDefenseMultiplier()));
        descLabel.setText("Style: " + selected.getDescription());
    }

    @FXML
    private void onApplyTactic() {
        int index = tacticComboBox.getSelectionModel().getSelectedIndex();
        if (index < 0)
            return;

        FootballTactic selected = tactics[index];
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
