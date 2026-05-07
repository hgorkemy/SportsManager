package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Tactic;
import com.sportsmanager.core.model.Team;
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
 * Works for all sports.
 * Tactics are loaded from Sport.getAvailableTactics() — no sport-specific imports needed.
 */
public class TacticsLineupController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private Pane         pitchPane;
    @FXML private ComboBox<Tactic> tacticCombo;
    @FXML private ListView<Player> playerList;
    @FXML private Label            lblStatus;
    @FXML private Button           btnConfirm;
    @FXML private Button           btnBack;

    // ── Pitch geometry ────────────────────────────────────────────────────────
    private static final double PAD = 28;
    private static final double PW  = 460;
    private static final double PH  = 620;
    private static final double FW  = PW - 2 * PAD;
    private static final double FH  = PH - 2 * PAD;

    // ── State ─────────────────────────────────────────────────────────────────

    private record SlotDef(String label, double relX, double relY) {}

    private static class SlotState {
        final SlotDef def;
        Player player;
        VBox   node;
        SlotState(SlotDef def) { this.def = def; }
    }

    private final List<SlotState>  slots     = new ArrayList<>();
    private final List<VBox>       slotNodes = new ArrayList<>();
    private ObservableList<Player> bench     = FXCollections.observableArrayList();

    private Player    selectedBenchPlayer = null;
    private Team      userTeam;
    private List<Tactic> sportTactics;
    private boolean   isHandball;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        userTeam     = session.getUserTeam();
        sportTactics = session.getSport().getAvailableTactics();
        isHandball   = session.getSport().getPlayersPerTeam() == 7;

        drawPitch();

        tacticCombo.getItems().setAll(sportTactics);
        tacticCombo.setCellFactory(lv -> tacticCell());
        tacticCombo.setButtonCell(tacticCell());
        tacticCombo.getSelectionModel().selectedItemProperty()
                   .addListener((obs, old, t) -> { if (t != null) applyTactic(t); });

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

        // Pre-select current tactic by name, fall back to first
        String currentName = userTeam.getCurrentTactic() != null
                ? userTeam.getCurrentTactic().getName() : "";
        Tactic toSelect = sportTactics.stream()
                .filter(t -> t.getName().equals(currentName))
                .findFirst()
                .orElse(sportTactics.isEmpty() ? null : sportTactics.get(0));
        if (toSelect != null) tacticCombo.setValue(toSelect); // triggers applyTactic

        GameSession.TacticsContext ctx = session.getTacticsContext();
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

    // ── Pitch / court drawing ─────────────────────────────────────────────────

    private void drawPitch() {
        Rectangle bg = new Rectangle(0, 0, PW, PH);
        bg.setFill(Color.web("#1a5276"));
        pitchPane.getChildren().add(bg);

        for (int i = 0; i < 8; i++) {
            Rectangle stripe = new Rectangle(0, PAD + i * (FH / 8), PW, FH / 8);
            stripe.setFill(i % 2 == 0 ? Color.web("#1a5c2e") : Color.web("#1d6633"));
            pitchPane.getChildren().add(stripe);
        }

        addRect(PAD, PAD, FW, FH);
        addLine(PAD, PH / 2, PAD + FW, PH / 2);
        addCircle(PW / 2, PH / 2, 52);
        addDot(PW / 2, PH / 2, 4);

        if (isHandball) {
            // 6-metre goal area arcs (top + bottom)
            drawHandballArc(PW / 2, PAD, 110, false);
            drawHandballArc(PW / 2, PAD + FH, 110, true);
            // 9-metre dashed line placeholder (simple rect)
            addDashedRect(PAD + FW * 0.15, PAD, FW * 0.70, FH * 0.28);
            addDashedRect(PAD + FW * 0.15, PAD + FH * 0.72, FW * 0.70, FH * 0.28);
        } else {
            double pbW = FW * 0.63, pbH = FH * 0.135;
            double pbX = PAD + (FW - pbW) / 2;
            addRect(pbX, PAD, pbW, pbH);
            double gaW = FW * 0.35, gaH = FH * 0.065;
            double gaX = PAD + (FW - gaW) / 2;
            addRect(gaX, PAD, gaW, gaH);
            addDot(PW / 2, PAD + FH * 0.115, 4);
            addRect(pbX, PAD + FH - pbH, pbW, pbH);
            addRect(gaX, PAD + FH - gaH, gaW, gaH);
            addDot(PW / 2, PAD + FH * 0.885, 4);
        }
    }

    private void drawHandballArc(double cx, double cy, double r, boolean upper) {
        Circle arc = new Circle(cx, cy, r);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.WHITE);
        arc.setStrokeWidth(1.5);
        // Clip so only the half facing the field is visible
        Rectangle clip = new Rectangle(cx - r - 5, upper ? cy - r - 5 : cy,
                                       (r + 5) * 2, r + 5);
        arc.setClip(clip);
        pitchPane.getChildren().add(arc);
    }

    private void addDashedRect(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.web("#ffffff88"));
        r.setStrokeWidth(1.2);
        r.getStrokeDashArray().addAll(8.0, 5.0);
        pitchPane.getChildren().add(r);
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

    private void applyTactic(Tactic tactic) {
        userTeam.setCurrentTactic(tactic);

        List<Player> assignedBefore;
        if (slots.isEmpty()) {
            assignedBefore = userTeam.getLineup().stream()
                    .filter(p -> !p.isInjured())
                    .collect(Collectors.toList());
        } else {
            assignedBefore = slots.stream()
                    .map(s -> s.player).filter(Objects::nonNull).collect(Collectors.toList());
        }

        pitchPane.getChildren().removeAll(slotNodes);
        slotNodes.clear();
        slots.clear();

        for (SlotDef def : formationFor(tactic.getName())) {
            slots.add(new SlotState(def));
        }

        List<Player> healthy = userTeam.getSquad().stream()
                .filter(p -> !p.isInjured()).collect(Collectors.toList());

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
            case "GK"                           -> code.equals("GK");
            // Football outfield
            case "LB", "RB", "LWB", "RWB",
                 "CB", "LCB", "RCB"             -> code.equals("DEF");
            case "LM", "RM", "CM", "DM", "AM"  -> code.equals("MID");
            case "LW", "RW", "CF", "ST"         -> code.equals("FWD");
            // Handball outfield
            case "LWG", "RWG"                   -> code.equals("WING");
            case "LB2", "CB2", "RB2", "LCB2",
                 "RCB2"                          -> code.equals("BACK");
            case "PIV"                           -> code.equals("PIV");
            default                              -> false;
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

            double px = PAD + slot.def.relX() * FW;
            double py = PAD + (1.0 - slot.def.relY()) * FH;

            node.setLayoutX(px - 34);   // VBox is fixed 68px wide → exact center
            node.setLayoutY(py - 24);   // jersey is 48px → center at py

            final int idx = i;
            node.setOnMouseClicked(e -> onSlotClicked(idx));
            node.setStyle("-fx-cursor: hand;");

            pitchPane.getChildren().add(node);
            slotNodes.add(node);
            slot.node = node;
        }
    }

    private VBox buildSlotNode(SlotState slot) {
        boolean hasPlayer = slot.player != null;
        boolean outOfPos  = hasPlayer && !matchesSlot(slot.player, slot.def.label());

        StackPane jersey = new StackPane();
        jersey.setPrefSize(54, 48);
        jersey.setMaxSize(54, 48);

        Rectangle body = new Rectangle(54, 48);
        body.setArcWidth(10); body.setArcHeight(10);
        body.setFill(Color.web(hasPlayer ? "#7f1d1d" : "#1e3a5f"));
        body.setStroke(Color.web(outOfPos ? "#ef4444" : "#fbbf24"));
        body.setStrokeWidth(2);

        Rectangle collar = new Rectangle(34, 10);
        collar.setFill(Color.web("#f59e0b"));
        collar.setArcWidth(6); collar.setArcHeight(6);
        StackPane.setAlignment(collar, Pos.TOP_CENTER);

        // Display a readable label: strip the numeric suffix used for uniqueness
        String displayLabel = slot.def.label().replaceAll("\\d+$", "");
        Text posText = new Text(displayLabel);
        posText.setFill(Color.WHITE);
        posText.setFont(Font.font("System", FontWeight.BOLD, 12));

        jersey.getChildren().addAll(body, collar, posText);

        if (outOfPos) {
            Text warning = new Text("⚠");
            warning.setFill(Color.web("#fbbf24"));
            warning.setFont(Font.font("System", FontWeight.BOLD, 13));
            StackPane.setAlignment(warning, Pos.TOP_RIGHT);
            warning.setTranslateX(-3);
            warning.setTranslateY(3);
            jersey.getChildren().add(warning);
        }

        String nameStr = hasPlayer
                ? slot.player.getFirstName().charAt(0) + ". " + slot.player.getLastName()
                : "";
        Label nameLabel = new Label(nameStr);
        nameLabel.setStyle("-fx-text-fill: " + (outOfPos ? "#fca5a5" : "white")
                           + "; -fx-font-size: 10px;");
        nameLabel.setMaxWidth(68);
        nameLabel.setPrefWidth(68);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);

        Label ovrLabel = new Label(hasPlayer ? "OVR:" + slot.player.getOverallRating() : "");
        ovrLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 9px; -fx-font-weight: bold;");
        ovrLabel.setMaxWidth(68);
        ovrLabel.setPrefWidth(68);
        ovrLabel.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(2, jersey, nameLabel, ovrLabel);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setMinWidth(68);
        vbox.setMaxWidth(68);
        return vbox;
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    private void onBenchPlayerClicked() {
        Player clicked = playerList.getSelectionModel().getSelectedItem();
        if (clicked == null) return;
        selectedBenchPlayer = (clicked == selectedBenchPlayer) ? null : clicked;
        playerList.refresh();
    }

    private void onSlotClicked(int idx) {
        SlotState slot = slots.get(idx);

        if (selectedBenchPlayer != null) {
            if (selectedBenchPlayer.isInjured()) {
                lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblStatus.setText("⚠ " + selectedBenchPlayer.getFirstName() + " "
                        + selectedBenchPlayer.getLastName() + " is injured and cannot play.");
                selectedBenchPlayer = null;
                playerList.getSelectionModel().clearSelection();
                playerList.refresh();
                return;
            }
            Player displaced = slot.player;
            slot.player = selectedBenchPlayer;
            bench.remove(selectedBenchPlayer);
            if (displaced != null) bench.add(displaced);
            selectedBenchPlayer = null;
            playerList.getSelectionModel().clearSelection();
        } else if (slot.player != null) {
            bench.add(slot.player);
            slot.player = null;
        }

        renderSlots();
        updateStatus();
    }

    // ── Auto-fill ─────────────────────────────────────────────────────────────

    @FXML
    private void onAutoFill() {
        for (SlotState s : slots) {
            if (s.player != null) { bench.add(s.player); s.player = null; }
        }
        for (SlotState slot : slots) {
            Player best = bench.stream()
                    .filter(p -> matchesSlot(p, slot.def.label()))
                    .findFirst().orElse(null);
            if (best == null) best = bench.isEmpty() ? null : bench.get(0);
            if (best != null) { slot.player = best; bench.remove(best); }
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

        long filled = slots.stream().filter(s -> s.player != null).count();
        int  total  = slots.size();
        if (filled < total) {
            lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("⚠ " + (total - filled) + " position(s) empty — fill all slots.");
            return;
        }
        if (lineup.stream().anyMatch(Player::isInjured)) {
            lineup.stream().filter(Player::isInjured).findFirst().ifPresent(p -> {
                lblStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblStatus.setText("⚠ " + p.getFirstName() + " " + p.getLastName()
                        + " is injured — remove from lineup.");
            });
            return;
        }
        boolean hasGK = lineup.stream().anyMatch(p ->
                p.getPosition() != null && "GK".equals(p.getPosition().getCode()));
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
                case MID_MATCH -> SportsManagerApp.navigateTo("MatchView");
                case BROWSE    -> SportsManagerApp.navigateTo("DashboardView");
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
        int  total  = slots.size();
        if (filled == total) {
            lblStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("✓ All " + total + " positions filled — ready!");
        } else {
            lblStatus.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
            lblStatus.setText(filled + " / " + total + " positions filled");
        }
    }

    private ListCell<Tactic> tacticCell() {
        return new ListCell<>() {
            @Override protected void updateItem(Tactic t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setText(null); return; }
                setText(t.getName() + "  —  " + t.getDescription());
            }
        };
    }

    // ── Formation definitions ─────────────────────────────────────────────────

    private List<SlotDef> formationFor(String name) {
        return switch (name) {
            // ── Football ──────────────────────────────────────────────────────
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
                new SlotDef("GK",   0.50, 0.05),
                new SlotDef("LWB",  0.05, 0.30), new SlotDef("LCB", 0.25, 0.21),
                new SlotDef("CB",   0.50, 0.19), new SlotDef("RCB", 0.75, 0.21),
                new SlotDef("RWB",  0.95, 0.30),
                new SlotDef("CM",   0.25, 0.56), new SlotDef("CM",  0.50, 0.56),
                new SlotDef("CM",   0.75, 0.56),
                new SlotDef("ST",   0.36, 0.83), new SlotDef("ST",  0.64, 0.83)
            );
            // ── Handball ──────────────────────────────────────────────────────
            // 3-2-1: GK + 3 backs + 2 wings + 1 pivot
            case "3-2-1" -> List.of(
                new SlotDef("GK",   0.50, 0.05),
                new SlotDef("LWG",  0.10, 0.60), new SlotDef("RWG",  0.90, 0.60),
                new SlotDef("LB2",  0.25, 0.75), new SlotDef("CB2",  0.50, 0.78),
                new SlotDef("RB2",  0.75, 0.75),
                new SlotDef("PIV",  0.50, 0.90)
            );
            // 4-2: GK + 4 backs + 2 wings
            case "4-2" -> List.of(
                new SlotDef("GK",   0.50, 0.05),
                new SlotDef("LWG",  0.08, 0.62), new SlotDef("RWG",  0.92, 0.62),
                new SlotDef("LB2",  0.20, 0.78), new SlotDef("LCB2", 0.40, 0.82),
                new SlotDef("RCB2", 0.60, 0.82), new SlotDef("RB2",  0.80, 0.78)
            );
            // 6-0: GK + 6 backs (defensive wall)
            case "6-0" -> List.of(
                new SlotDef("GK",   0.50, 0.05),
                new SlotDef("LWG",  0.05, 0.70), new SlotDef("LB2",  0.22, 0.78),
                new SlotDef("LCB2", 0.38, 0.83), new SlotDef("RCB2", 0.62, 0.83),
                new SlotDef("RB2",  0.78, 0.78), new SlotDef("RWG",  0.95, 0.70)
            );
            default -> isHandball ? formationFor("3-2-1") : formationFor("4-4-2");
        };
    }
}
