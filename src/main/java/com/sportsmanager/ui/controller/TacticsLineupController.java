package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballTactic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Combines tactics selection and lineup building in one screen.
 * Centre = football pitch with formation slots.
 * Right  = bench player list + tactic ComboBox.
 *
 * Interaction:
 *   1. Click a player on the bench list → selects them (highlighted).
 *   2. Click a slot on the pitch → assigns the selected player there.
 *      If the slot was occupied the old player returns to bench.
 *   3. Click an occupied slot with no bench selection → unassigns player.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class TacticsLineupController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private Pane             pitchPane;
    @FXML private ComboBox<FootballTactic> tacticCombo;
    @FXML private ListView<Player> playerList;
    @FXML private Label            lblStatus;
    @FXML private Button           btnConfirm;
    @FXML private Button           btnBack;

    // ── Pitch geometry ────────────────────────────────────────────────────────
    private static final double PAD  = 28;       // padding around field lines
    private static final double PW   = 460;      // pitchPane width
    private static final double PH   = 620;      // pitchPane height
    private static final double FW   = PW - 2*PAD;   // 404
    private static final double FH   = PH - 2*PAD;   // 564

    // ── State ─────────────────────────────────────────────────────────────────

    private record SlotDef(String label, double relX, double relY) {}

    private static class SlotState {
        final SlotDef def;
        Player player;
        VBox   node;
        SlotState(SlotDef def) { this.def = def; }
    }

    private final List<SlotState>  slots       = new ArrayList<>();
    private final List<VBox>       slotNodes   = new ArrayList<>();
    private ObservableList<Player> bench       = FXCollections.observableArrayList();

    private Player selectedBenchPlayer = null;  // highlighted player from bench
    private Team   userTeam;

    // ── Available tactics ─────────────────────────────────────────────────────
    private static final List<FootballTactic> TACTICS = List.of(
        FootballTactic.balanced(),   // 4-4-2
        FootballTactic.offensive(),  // 4-3-3
        FootballTactic.control(),    // 4-2-3-1
        FootballTactic.defensive()   // 5-3-2
    );

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        userTeam = GameSession.getInstance().getUserTeam();

        // Pitch background + markings (static, drawn once)
        drawPitch();

        // Tactic ComboBox
        tacticCombo.getItems().setAll(TACTICS);
        tacticCombo.setCellFactory(lv -> tacticCell());
        tacticCombo.setButtonCell(tacticCell());
        tacticCombo.getSelectionModel().selectedItemProperty()
                   .addListener((obs, old, t) -> { if (t != null) applyTactic(t); });

        // Bench list cell factory — injured players shown in red, selected in amber
        playerList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); setStyle(""); return; }
                if (p.isInjured()) {
                    setText("⚠ " + p.getFullName()
                            + " [" + (p.getPosition() != null ? p.getPosition().getCode() : "?") + "]"
                            + " OVR:" + p.getOverallRating()
                            + "  — INJURED (" + p.getInjuredGamesRemaining() + " games)");
                    setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                } else if (p == selectedBenchPlayer) {
                    setText(p.toString());
                    setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
                } else {
                    setText(p.toString());
                    setStyle("");
                }
            }
        });
        playerList.setOnMouseClicked(e -> onBenchPlayerClicked());

        // Select current tactic by matching name against the items list so the
        // cell factory always finds a proper item (avoids toString() fallback).
        String currentName = (userTeam.getCurrentTactic() instanceof FootballTactic ft)
                ? ft.getName() : "4-4-2";
        FootballTactic toSelect = TACTICS.stream()
                .filter(t -> t.getName().equals(currentName))
                .findFirst()
                .orElse(TACTICS.get(0));
        tacticCombo.setValue(toSelect);   // triggers applyTactic

        // Adapt button labels to where we came from
        GameSession.TacticsContext ctx = GameSession.getInstance().getTacticsContext();
        switch (ctx) {
            case MID_MATCH -> {
                btnConfirm.setText("✓  Apply Changes");
                btnBack.setText("← Back to Match");
            }
            case BROWSE -> {
                btnConfirm.setText("✓  Save Tactic");
                btnBack.setText("← Back to Dashboard");
            }
            default -> {
                btnConfirm.setText("✓  Confirm Lineup");
                btnBack.setText("← Back to Dashboard");
            }
        }
    }

    // ── Pitch drawing ─────────────────────────────────────────────────────────

    private void drawPitch() {
        // Background
        Rectangle bg = new Rectangle(0, 0, PW, PH);
        bg.setFill(Color.web("#1a5276"));
        pitchPane.getChildren().add(bg);

        // Grass stripes (subtle)
        for (int i = 0; i < 8; i++) {
            Rectangle stripe = new Rectangle(0, PAD + i * (FH / 8), PW, FH / 8);
            stripe.setFill(i % 2 == 0 ? Color.web("#1a5c2e") : Color.web("#1d6633"));
            pitchPane.getChildren().add(stripe);
        }

        // --- White line helpers ---
        // Outer boundary
        addRect(PAD, PAD, FW, FH);

        // Centre line
        addLine(PAD, PH / 2, PAD + FW, PH / 2);

        // Centre circle
        addCircle(PW / 2, PH / 2, 52);
        addDot(PW / 2, PH / 2, 4);

        // Top penalty box
        double pbW = FW * 0.63, pbH = FH * 0.135;
        double pbX = PAD + (FW - pbW) / 2;
        addRect(pbX, PAD, pbW, pbH);

        // Top goal area
        double gaW = FW * 0.35, gaH = FH * 0.065;
        double gaX = PAD + (FW - gaW) / 2;
        addRect(gaX, PAD, gaW, gaH);

        // Top penalty spot
        addDot(PW / 2, PAD + FH * 0.115, 4);

        // Bottom penalty box
        addRect(pbX, PAD + FH - pbH, pbW, pbH);

        // Bottom goal area
        addRect(gaX, PAD + FH - gaH, gaW, gaH);

        // Bottom penalty spot
        addDot(PW / 2, PAD + FH * 0.885, 4);
    }

    private void addRect(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.WHITE);
        r.setStrokeWidth(1.5);
        pitchPane.getChildren().add(r);
    }

    private void addLine(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(Color.WHITE);
        l.setStrokeWidth(1.5);
        pitchPane.getChildren().add(l);
    }

    private void addCircle(double cx, double cy, double r) {
        Circle c = new Circle(cx, cy, r);
        c.setFill(Color.TRANSPARENT);
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(1.5);
        pitchPane.getChildren().add(c);
    }

    private void addDot(double cx, double cy, double r) {
        Circle c = new Circle(cx, cy, r, Color.WHITE);
        pitchPane.getChildren().add(c);
    }

    // ── Formation / Tactic ────────────────────────────────────────────────────

    private void applyTactic(FootballTactic tactic) {
        userTeam.setCurrentTactic(tactic);

        // Collect currently assigned players before clearing.
        // On the very first call (slots still empty) seed from the team's saved lineup
        // so mid-match changes and pre-match edits both start from the existing selection.
        List<Player> assignedBefore;
        if (slots.isEmpty()) {
            // Seed from saved lineup but exclude injured players — they go to bench
            assignedBefore = userTeam.getLineup().stream()
                    .filter(p -> !p.isInjured())
                    .collect(Collectors.toList());
        } else {
            assignedBefore = slots.stream()
                    .map(s -> s.player).filter(Objects::nonNull).collect(Collectors.toList());
        }

        // Remove old slot nodes from pitch
        pitchPane.getChildren().removeAll(slotNodes);
        slotNodes.clear();
        slots.clear();

        // Build new slots
        for (SlotDef def : formationFor(tactic.getName())) {
            slots.add(new SlotState(def));
        }

        // Rebuild bench: healthy squad players not yet in a slot
        List<Player> healthy = userTeam.getSquad().stream()
                .filter(p -> !p.isInjured()).collect(Collectors.toList());

        // Auto-preserve old assignments where labels match
        for (SlotState slot : slots) {
            for (int i = 0; i < assignedBefore.size(); i++) {
                Player p = assignedBefore.get(i);
                if (p != null && matchesSlot(p, slot.def.label())) {
                    slot.player = p;
                    assignedBefore.set(i, null);
                    break;
                }
            }
        }

        // Bench = healthy players not assigned to any slot
        List<Player> assigned = slots.stream().map(s -> s.player)
                .filter(Objects::nonNull).collect(Collectors.toList());
        bench = FXCollections.observableArrayList(
                healthy.stream().filter(p -> !assigned.contains(p)).collect(Collectors.toList()));
        playerList.setItems(bench);
        selectedBenchPlayer = null;

        renderSlots();
        updateStatus();
    }

    private boolean matchesSlot(Player p, String slotLabel) {
        if (p.getPosition() == null) return false;
        String code = p.getPosition().getCode();
        return switch (slotLabel) {
            case "GK"                       -> code.equals("GK");
            case "LB", "RB", "LWB", "RWB"  -> code.equals("DEF");
            case "CB", "LCB", "RCB"         -> code.equals("DEF");
            case "LM", "RM", "CM", "DM", "AM" -> code.equals("MID");
            case "LW", "RW", "CF", "ST"     -> code.equals("FWD");
            default                          -> false;
        };
    }

    // ── Slot rendering ────────────────────────────────────────────────────────

    private void renderSlots() {
        pitchPane.getChildren().removeAll(slotNodes);
        slotNodes.clear();

        for (int i = 0; i < slots.size(); i++) {
            SlotState slot = slots.get(i);
            VBox node = buildSlotNode(slot);
            slot.node = node;

            // Pixel position on pitch
            double px = PAD + slot.def.relX() * FW;
            double py = PAD + (1.0 - slot.def.relY()) * FH;

            node.setLayoutX(px - 34);   // centre the 68px-wide node
            node.setLayoutY(py - 28);   // centre the jersey

            final int idx = i;
            node.setOnMouseClicked(e -> onSlotClicked(idx));
            node.setStyle("-fx-cursor: hand;");

            pitchPane.getChildren().add(node);
            slotNodes.add(node);
            slot.node = node;
        }
    }

    private VBox buildSlotNode(SlotState slot) {
        boolean hasPlayer  = slot.player != null;
        boolean outOfPos   = hasPlayer && !matchesSlot(slot.player, slot.def.label());

        // Jersey stack
        StackPane jersey = new StackPane();
        jersey.setPrefSize(54, 48);
        jersey.setMaxSize(54, 48);

        // Jersey body — red border when out of position, gold otherwise
        Rectangle body = new Rectangle(54, 48);
        body.setArcWidth(10); body.setArcHeight(10);
        body.setFill(Color.web(hasPlayer ? "#7f1d1d" : "#1e3a5f"));
        body.setStroke(Color.web(outOfPos ? "#ef4444" : "#fbbf24"));
        body.setStrokeWidth(2);

        // Yellow collar band (top strip)
        Rectangle collar = new Rectangle(34, 10);
        collar.setFill(Color.web("#f59e0b"));
        collar.setArcWidth(6); collar.setArcHeight(6);
        StackPane.setAlignment(collar, Pos.TOP_CENTER);

        // Position label (e.g. "GK", "CB")
        Text posText = new Text(slot.def.label());
        posText.setFill(Color.WHITE);
        posText.setFont(Font.font("System", FontWeight.BOLD, 12));

        jersey.getChildren().addAll(body, collar, posText);

        // ⚠ Out-of-position badge — top-right corner of the jersey
        if (outOfPos) {
            Text warning = new Text("⚠");
            warning.setFill(Color.web("#fbbf24"));
            warning.setFont(Font.font("System", FontWeight.BOLD, 13));
            StackPane.setAlignment(warning, Pos.TOP_RIGHT);
            warning.setTranslateX(-3);
            warning.setTranslateY(3);
            jersey.getChildren().add(warning);
        }

        // Player name below — light-red tint when out of position
        String nameStr = hasPlayer
                ? slot.player.getFirstName().charAt(0) + ". " + slot.player.getLastName()
                : "";
        Label nameLabel = new Label(nameStr);
        nameLabel.setStyle("-fx-text-fill: " + (outOfPos ? "#fca5a5" : "white")
                           + "; -fx-font-size: 10px;");
        nameLabel.setMaxWidth(80);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);

        VBox vbox = new VBox(2, jersey, nameLabel);
        vbox.setAlignment(Pos.TOP_CENTER);
        return vbox;
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    private void onBenchPlayerClicked() {
        Player clicked = playerList.getSelectionModel().getSelectedItem();
        if (clicked == null) return;

        if (clicked == selectedBenchPlayer) {
            // Deselect
            selectedBenchPlayer = null;
        } else {
            selectedBenchPlayer = clicked;
        }
        playerList.refresh();       // repaint to show highlight
    }

    private void onSlotClicked(int idx) {
        SlotState slot = slots.get(idx);

        if (selectedBenchPlayer != null) {
            // Block injured players
            if (selectedBenchPlayer.isInjured()) {
                lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblStatus.setText("⚠ " + selectedBenchPlayer.getFirstName() + " " + selectedBenchPlayer.getLastName()
                        + " is injured and cannot play.");
                selectedBenchPlayer = null;
                playerList.getSelectionModel().clearSelection();
                playerList.refresh();
                return;
            }

            // Assign selectedBenchPlayer to this slot
            Player displaced = slot.player;

            slot.player = selectedBenchPlayer;
            bench.remove(selectedBenchPlayer);

            if (displaced != null) bench.add(displaced);    // return old player to bench

            selectedBenchPlayer = null;
            playerList.getSelectionModel().clearSelection();

        } else if (slot.player != null) {
            // No bench selection → unassign slot
            bench.add(slot.player);
            slot.player = null;
        }

        renderSlots();
        updateStatus();
    }

    // ── Auto-fill ─────────────────────────────────────────────────────────────

    @FXML
    private void onAutoFill() {
        // Clear all slots first
        for (SlotState s : slots) {
            if (s.player != null) { bench.add(s.player); s.player = null; }
        }

        // Assign by position preference
        for (SlotState slot : slots) {
            // Find best matching bench player
            Player best = bench.stream()
                    .filter(p -> matchesSlot(p, slot.def.label()))
                    .findFirst().orElse(null);

            if (best == null) {
                // Fallback: take any bench player
                best = bench.isEmpty() ? null : bench.get(0);
            }

            if (best != null) {
                slot.player = best;
                bench.remove(best);
            }
        }

        renderSlots();
        updateStatus();
    }

    // ── Confirm ───────────────────────────────────────────────────────────────

    @FXML
    private void onConfirm() {
        List<Player> lineup = slots.stream()
                .map(s -> s.player)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Pre-validate with clear messages before calling setLineup
        long filled = slots.stream().filter(s -> s.player != null).count();
        int  total  = slots.size();
        if (filled < total) {
            lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("⚠ " + (total - filled) + " position(s) empty — fill all slots.");
            return;
        }
        lineup.stream().filter(Player::isInjured).findFirst().ifPresent(p -> {
            lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("⚠ " + p.getFirstName() + " " + p.getLastName() + " is injured — remove from lineup.");
        });
        if (lineup.stream().anyMatch(Player::isInjured)) return;

        boolean hasGK = lineup.stream().anyMatch(p ->
                p.getPosition() instanceof FootballPosition fp && fp == FootballPosition.GOALKEEPER);
        if (!hasGK) {
            lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("⚠ No goalkeeper in lineup — assign a GK to the GK slot.");
            return;
        }

        try {
            userTeam.setLineup(lineup);
            GameSession session = GameSession.getInstance();
            switch (session.getTacticsContext()) {
                case PRE_MATCH -> {
                    session.getMatchEngine().resetMatch();
                    SportsManagerApp.navigateTo("MatchView");
                }
                case MID_MATCH -> {
                    // No engine reset — changes take effect immediately in ongoing match
                    SportsManagerApp.navigateTo("MatchView");
                }
                case BROWSE -> {
                    // Just save tactic/lineup for later, return to dashboard
                    SportsManagerApp.navigateTo("DashboardView");
                }
            }
        } catch (IllegalArgumentException e) {
            lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("⚠ " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        GameSession.TacticsContext ctx = GameSession.getInstance().getTacticsContext();
        if (ctx == GameSession.TacticsContext.MID_MATCH) {
            SportsManagerApp.navigateTo("MatchView");
        } else {
            SportsManagerApp.navigateTo("DashboardView");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateStatus() {
        long filled = slots.stream().filter(s -> s.player != null).count();
        int total   = slots.size();
        if (filled == total) {
            lblStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("✓ All " + total + " positions filled — ready!");
        } else {
            lblStatus.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
            lblStatus.setText(filled + " / " + total + " positions filled");
        }
    }

    private ListCell<FootballTactic> tacticCell() {
        return new ListCell<>() {
            @Override protected void updateItem(FootballTactic t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setText(null); return; }
                setText(t.getName() + "  —  " + t.getDescription());
            }
        };
    }

    // ── Formation definitions ─────────────────────────────────────────────────

    private List<SlotDef> formationFor(String name) {
        return switch (name) {
            case "4-4-2" -> List.of(
                new SlotDef("GK",  0.50, 0.05),
                new SlotDef("LB",  0.12, 0.23), new SlotDef("CB",  0.37, 0.21),
                new SlotDef("CB",  0.63, 0.21), new SlotDef("RB",  0.88, 0.23),
                new SlotDef("LM",  0.08, 0.53), new SlotDef("CM",  0.35, 0.51),
                new SlotDef("CM",  0.65, 0.51), new SlotDef("RM",  0.92, 0.53),
                new SlotDef("ST",  0.36, 0.83), new SlotDef("ST",  0.64, 0.83)
            );
            case "4-3-3" -> List.of(
                new SlotDef("GK",  0.50, 0.05),
                new SlotDef("LB",  0.12, 0.23), new SlotDef("CB",  0.37, 0.21),
                new SlotDef("CB",  0.63, 0.21), new SlotDef("RB",  0.88, 0.23),
                new SlotDef("CM",  0.24, 0.53), new SlotDef("CM",  0.50, 0.53),
                new SlotDef("CM",  0.76, 0.53),
                new SlotDef("LW",  0.12, 0.83), new SlotDef("CF",  0.50, 0.86),
                new SlotDef("RW",  0.88, 0.83)
            );
            case "4-2-3-1" -> List.of(
                new SlotDef("GK",  0.50, 0.05),
                new SlotDef("LB",  0.12, 0.23), new SlotDef("CB",  0.37, 0.21),
                new SlotDef("CB",  0.63, 0.21), new SlotDef("RB",  0.88, 0.23),
                new SlotDef("DM",  0.36, 0.44), new SlotDef("DM",  0.64, 0.44),
                new SlotDef("LW",  0.11, 0.65), new SlotDef("AM",  0.50, 0.65),
                new SlotDef("RW",  0.89, 0.65),
                new SlotDef("CF",  0.50, 0.86)
            );
            case "5-3-2" -> List.of(
                new SlotDef("GK",  0.50, 0.05),
                new SlotDef("LWB", 0.05, 0.30), new SlotDef("LCB", 0.25, 0.21),
                new SlotDef("CB",  0.50, 0.19), new SlotDef("RCB", 0.75, 0.21),
                new SlotDef("RWB", 0.95, 0.30),
                new SlotDef("CM",  0.25, 0.56), new SlotDef("CM",  0.50, 0.56),
                new SlotDef("CM",  0.75, 0.56),
                new SlotDef("ST",  0.36, 0.83), new SlotDef("ST",  0.64, 0.83)
            );
            default -> formationFor("4-4-2");
        };
    }
}
