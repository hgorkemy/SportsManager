package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.Coach;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.core.model.TrainingProgram;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Weekly training screen — shown when the user clicks "Next Week" on the Dashboard.
 *
 * Completely sport-agnostic: all training programs are fetched from the Coach subclass
 * via coach.getTrainingPrograms(). Adding a new sport only requires implementing
 * that method in the new Coach subclass — no changes here.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class TrainingController {

    @FXML private HBox   trainingCards;
    @FXML private Label  lblCoachName;
    @FXML private Label  lblCoachSpecialty;
    @FXML private Label  lblCoachSkill;
    @FXML private Label  lblStatus;
    @FXML private Button btnConfirm;

    // ── State ─────────────────────────────────────────────────────────────────

    private Team                 userTeam;
    private Coach                coach;          // may be null if team has no coach
    private List<TrainingProgram> programs;
    private String               selectedName = null;
    private final List<VBox>     cardNodes    = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        userTeam = session.getUserTeam();
        coach    = userTeam.getCoaches().isEmpty() ? null : userTeam.getCoaches().get(0);

        // Programs come entirely from the coach — controller knows nothing about the sport
        programs = (coach != null)
                ? coach.getTrainingPrograms()
                : List.of(); // no coach → skip training is the only option

        // Coach info panel
        if (coach != null) {
            lblCoachName.setText(coach.getFullName());
            lblCoachSpecialty.setText(
                coach.getSpecialty() + " Specialist  ·  " + coach.getExperience() + " yrs experience");
            lblCoachSkill.setText(
                "Coaching effectiveness: " + coach.calculateCoachingEffectiveness() + "%");
        } else {
            lblCoachName.setText("No Coach Assigned");
            lblCoachSpecialty.setText("Hire a coach to unlock focused training");
            lblCoachSkill.setText("Effectiveness: —");
        }

        buildCards();
        btnConfirm.setDisable(true);
    }

    // ── Card building ─────────────────────────────────────────────────────────

    private void buildCards() {
        trainingCards.getChildren().clear();
        cardNodes.clear();

        for (int i = 0; i < programs.size(); i++) {
            TrainingProgram p   = programs.get(i);
            boolean isSpecialty = coach != null && coach.getSpecialty().equals(p.name());
            VBox card = buildCard(p, isSpecialty, false);
            final int idx = i;
            card.setOnMouseClicked(e -> selectProgram(idx));
            card.setCursor(Cursor.HAND);
            trainingCards.getChildren().add(card);
            cardNodes.add(card);
        }

        if (programs.isEmpty()) {
            Label none = new Label("No training programs available — assign a coach first.");
            none.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            trainingCards.getChildren().add(none);
        }
    }

    private VBox buildCard(TrainingProgram p, boolean isSpecialty, boolean selected) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(172);
        card.setMinHeight(200);
        card.setStyle(cardStyle(selected, isSpecialty));

        Label emoji = new Label(p.emoji());
        emoji.setStyle("-fx-font-size: 34px;");

        Label name = new Label(p.name() + " Training");
        name.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setTextAlignment(TextAlignment.CENTER);

        Label desc = new Label(p.description());
        desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        desc.setWrapText(true);
        desc.setMaxWidth(150);
        desc.setTextAlignment(TextAlignment.CENTER);

        // Attribute gains — built from the program's own bonuses map, no hardcoding
        StringBuilder sb = new StringBuilder();
        p.bonuses().forEach((attr, val) -> sb.append("+").append(val).append(" ").append(attr).append("  "));
        Label bonusLabel = new Label(sb.toString().trim());
        bonusLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 11px;");
        bonusLabel.setWrapText(true);
        bonusLabel.setMaxWidth(150);
        bonusLabel.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(emoji, name, desc, bonusLabel);

        if (isSpecialty) {
            Label badge = new Label("⭐ Coach specialty  ×1.5");
            badge.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 10px; -fx-font-weight: bold;");
            badge.setWrapText(true);
            badge.setTextAlignment(TextAlignment.CENTER);
            card.getChildren().add(badge);
        }

        return card;
    }

    private String cardStyle(boolean selected, boolean specialty) {
        String bg     = selected  ? "#1c1a05"  : "#1e293b";
        String border = selected  ? "#f59e0b"
                      : specialty ? "#3b82f6"
                      :             "#334155";
        return String.format(
            "-fx-background-color: %s; -fx-background-radius: 12; " +
            "-fx-border-color: %s; -fx-border-radius: 12; " +
            "-fx-border-width: 2; -fx-padding: 16;", bg, border);
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    private void selectProgram(int idx) {
        selectedName = programs.get(idx).name();

        // Rebuild all cards so only the selected one is highlighted
        for (int i = 0; i < programs.size(); i++) {
            boolean specialty = coach != null && coach.getSpecialty().equals(programs.get(i).name());
            VBox newCard = buildCard(programs.get(i), specialty, i == idx);
            final int fi = i;
            newCard.setOnMouseClicked(e -> selectProgram(fi));
            newCard.setCursor(Cursor.HAND);
            trainingCards.getChildren().set(i, newCard);
            cardNodes.set(i, newCard);
        }

        boolean specialtyMatch = coach != null && coach.getSpecialty().equals(selectedName);
        String extra = specialtyMatch ? "  ⭐ Coach specialty — ×1.5 bonus!" : "";
        lblStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblStatus.setText("✓ " + selectedName + " Training selected." + extra);
        btnConfirm.setDisable(false);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @FXML
    private void onConfirm() {
        if (selectedName == null || coach == null) return;

        // Find the chosen program and apply it — all logic lives in the Coach class
        programs.stream()
                .filter(p -> p.name().equals(selectedName))
                .findFirst()
                .ifPresent(program ->
                    userTeam.getHealthyPlayers()
                            .forEach(player -> coach.conductTraining(player, program))
                );

        advanceAndReturn();
    }

    @FXML
    private void onSkip() {
        advanceAndReturn();
    }

    // ── Week advance ──────────────────────────────────────────────────────────

    private void advanceAndReturn() {
        GameSession session = GameSession.getInstance();
        session.getLeague().advanceWeek();     // auto-trains all AI teams via their coaches
        session.setMatchPlayedThisWeek(false);
        SportsManagerApp.navigateTo("DashboardView");
    }
}
