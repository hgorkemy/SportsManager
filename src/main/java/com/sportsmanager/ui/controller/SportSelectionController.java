package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import com.sportsmanager.core.model.GameSession;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class SportSelectionController {

    @FXML private StackPane rootPane;

    // ── Panels ────────────────────────────────────────────────────────────────
    private VBox leftPane;
    private VBox rightPane;
    private Rectangle dividerLine;
    private StackPane overlay;

    private Label leftClickLabel;
    private Label rightClickLabel;

    // ── Animation state ───────────────────────────────────────────────────────
    private double dividerPos = 0.5;   // current rendered position (0..1)
    private double targetPos  = 0.5;   // where the mouse is pointing
    private AnimationTimer animTimer;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        buildUI();
        startAnimation();
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {

        // ── Left pane — Football ──────────────────────────────────────────────
        leftPane = new VBox(24);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.setCursor(Cursor.HAND);
        leftPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #052e16, #14532d, #166534);");

        Label leftEmoji = new Label("⚽");
        leftEmoji.setStyle("-fx-font-size: 80px;");

        Label leftTitle = new Label("FOOTBALL");
        leftTitle.setStyle(
            "-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: #f0fdf4;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 12, 0, 0, 3);");

        Label leftSub = new Label("11 players  ·  League simulation");
        leftSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #86efac;");

        leftClickLabel = new Label("▶   CLICK TO PLAY");
        leftClickLabel.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");
        leftClickLabel.setOpacity(0);

        leftPane.getChildren().addAll(leftEmoji, leftTitle, leftSub, leftClickLabel);
        leftPane.setOnMouseClicked(e -> onSportSelected("Football"));

        // ── Right pane — Handball ─────────────────────────────────────────────
        rightPane = new VBox(24);
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setCursor(Cursor.HAND);
        rightPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom left, #431407, #9a3412, #c2410c);");
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        rightPane.setMinWidth(0);

        Label rightEmoji = new Label("🤾");
        rightEmoji.setStyle("-fx-font-size: 80px;");

        Label rightTitle = new Label("HANDBALL");
        rightTitle.setStyle(
            "-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: #fff7ed;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 12, 0, 0, 3);");

        Label rightSub = new Label("7 players  ·  League simulation");
        rightSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #fdba74;");

        rightClickLabel = new Label("▶   CLICK TO PLAY");
        rightClickLabel.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #fb923c;");
        rightClickLabel.setOpacity(0);

        rightPane.getChildren().addAll(rightEmoji, rightTitle, rightSub, rightClickLabel);
        rightPane.setOnMouseClicked(e -> onSportSelected("Handball"));

        // ── HBox background container ─────────────────────────────────────────
        HBox container = new HBox(leftPane, rightPane);
        container.prefWidthProperty().bind(rootPane.widthProperty());
        container.prefHeightProperty().bind(rootPane.heightProperty());

        // ── Top title overlay ─────────────────────────────────────────────────
        Label appTitle = new Label("SPORTS MANAGER");
        appTitle.setStyle(
            "-fx-font-size: 38px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: rgba(255,255,255,0.82);" +
            "-fx-letter-spacing: 4px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 14, 0, 0, 3);");
        StackPane.setAlignment(appTitle, Pos.TOP_CENTER);
        appTitle.setTranslateY(32);

        // ── Divider line ──────────────────────────────────────────────────────
        dividerLine = new Rectangle(3, 0);
        dividerLine.setFill(Color.WHITE);
        dividerLine.heightProperty().bind(rootPane.heightProperty());
        DropShadow glow = new DropShadow(20, Color.WHITE);
        glow.setSpread(0.4);
        dividerLine.setEffect(glow);
        // CENTER alignment: default position = w/2. TranslateX = leftW - w/2 puts it exactly at the border.
        StackPane.setAlignment(dividerLine, Pos.CENTER);

        // ── Overlay (shown after click) ───────────────────────────────────────
        overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlay.prefWidthProperty().bind(rootPane.widthProperty());
        overlay.prefHeightProperty().bind(rootPane.heightProperty());

        // ── Mouse tracking ────────────────────────────────────────────────────
        rootPane.setOnMouseMoved(e -> {
            double w = rootPane.getWidth();
            if (w > 0) {
                // Formula: mouse left → left panel grows (targetPos > 0.5),
                //          mouse right → right panel grows (targetPos < 0.5).
                // Maps mouseX [0..w] → targetPos [0.70..0.30]
                targetPos = 0.5 + (0.5 - e.getX() / w) * 0.40;
            }

            boolean onLeft = e.getX() < rootPane.getWidth() / 2.0;
            leftClickLabel.setOpacity(onLeft ? 1.0 : 0.0);
            rightClickLabel.setOpacity(onLeft ? 0.0 : 1.0);
        });

        rootPane.getChildren().addAll(container, dividerLine, appTitle, overlay);
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    private void startAnimation() {
        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Smooth lerp toward target
                dividerPos += (targetPos - dividerPos) * 0.10;

                double w = rootPane.getWidth();
                if (w <= 0) return;

                double leftW = w * dividerPos;
                leftPane.setPrefWidth(leftW);

                // Shift the divider line: StackPane centres children,
                // so translate = leftW - half the total width
                dividerLine.setTranslateX(leftW - w / 2.0);

                // Dim the inactive side
                boolean mouseOnLeft = targetPos < 0.5;
                leftPane.setOpacity(mouseOnLeft ? 1.0 : 0.62);
                rightPane.setOpacity(mouseOnLeft ? 0.62 : 1.0);
            }
        };
        animTimer.start();
    }

    // ── Click handler ─────────────────────────────────────────────────────────

    private void onSportSelected(String sportName) {
        if (animTimer != null) animTimer.stop();

        GameSession.getInstance().setSelectedSportName(sportName);

        boolean isFootball   = "Football".equals(sportName);
        String  accentColor  = isFootball ? "#4ade80" : "#fb923c";
        String  bgAccent     = isFootball ? "#052e16" : "#431407";
        String  emoji        = isFootball ? "⚽" : "🤾";

        // ── Build overlay card ────────────────────────────────────────────────
        overlay.getChildren().clear();

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(340);
        card.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 2;" +
            "-fx-padding: 44 40;");

        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 54px;");

        Label titleLbl = new Label(sportName + " selected");
        titleLbl.setStyle(
            "-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label divider = new Label("────────────────");
        divider.setStyle("-fx-text-fill: #334155; -fx-font-size: 11px;");

        Button btnNew = buildOverlayButton(
            "▶  New Game", accentColor, "#0f172a", true);
        btnNew.setOnAction(e -> SportsManagerApp.navigateTo("TeamSelectionView"));

        Button btnLoad = buildOverlayButton(
            "📂  Load Save", "#1e293b", "#f8fafc", false);
        btnLoad.setOnAction(e -> SportsManagerApp.navigateTo("SavesView"));

        Button btnBack = new Button("← Back");
        btnBack.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #64748b;" +
            "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0;");
        btnBack.setOnAction(e -> {
            overlay.setVisible(false);
            dividerPos = targetPos;          // resume without jump
            if (animTimer != null) animTimer.start();
        });

        card.getChildren().addAll(emojiLbl, titleLbl, divider, btnNew, btnLoad, btnBack);
        overlay.getChildren().add(card);

        // Fade in
        overlay.setOpacity(0);
        overlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(220), overlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Button buildOverlayButton(String text, String bg, String fg, boolean bold) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            (bold ? "-fx-font-weight: bold;" : "") +
            "-fx-font-size: 15px;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 13 0;" +
            "-fx-cursor: hand;");
        return btn;
    }
}
