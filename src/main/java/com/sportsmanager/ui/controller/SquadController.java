package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Position;
import com.sportsmanager.core.model.Sport;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Squad management screen.
 * Fully sport-agnostic: filters and attributes are driven by the active Sport.
 */
public class SquadController {

    @FXML private TableView<Player> squadTable;
    @FXML private TableColumn<Player, String>  colName;
    @FXML private TableColumn<Player, String>  colPos;
    @FXML private TableColumn<Player, String>  colOvr;
    @FXML private TableColumn<Player, Integer> colGoals;
    @FXML private TableColumn<Player, Integer> colApps;
    @FXML private TableColumn<Player, String>  colStatus;

    @FXML private HBox filterBar;

    @FXML private Label lblPlayerName;
    @FXML private Label lblPlayerPos;
    @FXML private Label lblPlayerAge;
    @FXML private VBox  attributeRows;
    @FXML private Label lblGoals;
    @FXML private Label lblYellowCards;
    @FXML private Label lblRedCards;
    @FXML private Label lblAppearances;
    @FXML private Label lblInjury;

    private ObservableList<Player> allPlayers;
    private Sport sport;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        sport = session.getSport();
        List<Player> squad = session.getUserTeam().getSquad();
        allPlayers = FXCollections.observableArrayList(squad);

        colName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colPos.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getPosition() != null ? d.getValue().getPosition().getCode() : "?"));
        colOvr.setCellValueFactory(d -> {
            Player p = d.getValue();
            int delta = p.getOverallChange();
            String suffix = delta == 0 ? "" : (delta > 0 ? " (+" + delta + ")" : " (" + delta + ")");
            return new SimpleStringProperty(p.getOverallRating() + suffix);
        });
        colGoals.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getGoals()));
        colApps.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getAppearances()));
        colStatus.setCellValueFactory(d -> {
            Player p = d.getValue();
            if (p.isSuspended())
                return new SimpleStringProperty("🟥 Suspended (" + p.getSuspendedGamesRemaining() + "g)");
            if (p.isInjured())
                return new SimpleStringProperty("🤕 Injured (" + p.getInjuredGamesRemaining() + "g)");
            return new SimpleStringProperty("Fit");
        });

        squadTable.setItems(allPlayers);
        squadTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showDetails(selected));

        buildFilterButtons();
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private void buildFilterButtons() {
        filterBar.getChildren().clear();

        Button btnAll = new Button("All");
        btnAll.setOnAction(e -> applyFilter(null));
        filterBar.getChildren().add(btnAll);

        for (Position pos : sport.getAvailablePositions()) {
            Button btn = new Button(pos.getCode());
            btn.setOnAction(e -> applyFilter(pos.getCode()));
            filterBar.getChildren().add(btn);
        }
    }

    private void applyFilter(String code) {
        if (code == null) {
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
            attributeRows.getChildren().clear();
            lblGoals.setText("");
            lblYellowCards.setText("");
            lblRedCards.setText("");
            lblAppearances.setText("");
            lblInjury.setText("");
            return;
        }

        int delta = player.getOverallChange();
        String deltaStr = delta == 0 ? "" : (delta > 0 ? "  (+" + delta + ")" : "  (" + delta + ")");
        lblPlayerName.setText(player.getFullName() + "   OVR: " + player.getOverallRating() + deltaStr);
        lblPlayerPos.setText("Position:  " + (player.getPosition() != null ? player.getPosition().getDisplayName() : "?"));
        lblPlayerAge.setText("Age:       " + player.getAge());

        attributeRows.getChildren().clear();
        Map<String, Integer> attrs = player.getAttributes();
        for (String attrName : sport.getPlayerAttributes()) {
            Integer val = attrs.get(attrName);
            if (val == null) continue;
            String display = capitalize(attrName) + ":  " + val;
            Label lbl = new Label(display);
            lbl.setStyle("-fx-text-fill: #e2e8f0;");
            attributeRows.getChildren().add(lbl);
        }

        lblGoals.setText("Goals:         " + player.getGoals());
        lblYellowCards.setText("Yellow Cards:  " + player.getYellowCards());
        lblRedCards.setText("Red Cards:     " + player.getRedCards());
        lblAppearances.setText("Appearances:   " + player.getAppearances());
        if (player.isSuspended())
            lblInjury.setText("🟥 SUSPENDED — banned for " + player.getSuspendedGamesRemaining() + " game(s)");
        else if (player.isInjured())
            lblInjury.setText("INJURED — out for " + player.getInjuredGamesRemaining() + " game(s)");
        else
            lblInjury.setText("");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @FXML
    private void onBackToDashboard() {
        SportsManagerApp.navigateTo("DashboardView");
    }
}
