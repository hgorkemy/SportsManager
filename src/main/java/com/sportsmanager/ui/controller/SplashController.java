package com.sportsmanager.ui.controller;

import com.sportsmanager.SportsManagerApp;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Intro splash screen shown once at startup.
 *
 * Animation sequence:
 *  1. Sport icons (⚽ 🤾) slide in from sides + fade in
 *  2. "SPORTS MANAGER" rises up + fades in
 *  3. Subtitle "Developed by Dining Philosophers" fades in
 *  4. Brief hold, then whole screen fades out → SportSelectionView
 *
 * Click or press any key to skip.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class SplashController {

    @FXML private StackPane rootPane;

    private SequentialTransition fullSequence;
    private boolean navigated = false;

    @FXML
    public void initialize() {
        buildAndAnimate();

        // Click or key press → skip
        rootPane.setOnMouseClicked(e -> skip());
        rootPane.setOnKeyPressed(e -> skip());
        rootPane.setFocusTraversable(true);
        // Request focus after layout so key events work
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) rootPane.requestFocus();
        });
    }

    // ── Build layout & fire animation ─────────────────────────────────────────

    private void buildAndAnimate() {

        // ── Sport icons ───────────────────────────────────────────────────────
        Label footballIcon = new Label("⚽");
        footballIcon.setStyle("-fx-font-size: 68px;");
        footballIcon.setOpacity(0);

        Label handballIcon = new Label("🤾");
        handballIcon.setStyle("-fx-font-size: 68px;");
        handballIcon.setOpacity(0);

        HBox iconsRow = new HBox(60, footballIcon, handballIcon);
        iconsRow.setAlignment(Pos.CENTER);

        // ── Main title ────────────────────────────────────────────────────────
        Label title = new Label("SPORTS MANAGER");
        title.setStyle(
            "-fx-font-size: 58px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #f8fafc;" +
            "-fx-letter-spacing: 3px;" +
            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.6), 28, 0, 0, 0);");
        title.setOpacity(0);

        // ── Subtitle ──────────────────────────────────────────────────────────
        Label subtitle = new Label("Developed by  Dining Philosophers");
        subtitle.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: #64748b;" +
            "-fx-letter-spacing: 1px;");
        subtitle.setOpacity(0);

        // ── Skip hint ─────────────────────────────────────────────────────────
        Label skipHint = new Label("Click anywhere to skip");
        skipHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
        StackPane.setAlignment(skipHint, Pos.BOTTOM_CENTER);
        skipHint.setTranslateY(-24);
        skipHint.setOpacity(0);

        // ── Layout ────────────────────────────────────────────────────────────
        VBox center = new VBox(28, iconsRow, title, subtitle);
        center.setAlignment(Pos.CENTER);

        rootPane.getChildren().addAll(center, skipHint);

        // ── Animations ────────────────────────────────────────────────────────

        // Icons: slide in from opposite sides + fade
        TranslateTransition footballSlide = new TranslateTransition(Duration.millis(700), footballIcon);
        footballSlide.setFromX(-120);
        footballSlide.setToX(0);
        FadeTransition footballFade = new FadeTransition(Duration.millis(700), footballIcon);
        footballFade.setFromValue(0);
        footballFade.setToValue(1);
        ParallelTransition footballAnim = new ParallelTransition(footballSlide, footballFade);

        TranslateTransition handballSlide = new TranslateTransition(Duration.millis(700), handballIcon);
        handballSlide.setFromX(120);
        handballSlide.setToX(0);
        FadeTransition handballFade = new FadeTransition(Duration.millis(700), handballIcon);
        handballFade.setFromValue(0);
        handballFade.setToValue(1);
        ParallelTransition handballAnim = new ParallelTransition(handballSlide, handballFade);

        ParallelTransition iconsAnim = new ParallelTransition(footballAnim, handballAnim);

        // Title: rise up + fade in
        TranslateTransition titleRise = new TranslateTransition(Duration.millis(800), title);
        titleRise.setFromY(30);
        titleRise.setToY(0);
        FadeTransition titleFade = new FadeTransition(Duration.millis(800), title);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);
        ParallelTransition titleAnim = new ParallelTransition(titleRise, titleFade);

        // Subtitle: fade in
        FadeTransition subtitleFade = new FadeTransition(Duration.millis(700), subtitle);
        subtitleFade.setFromValue(0);
        subtitleFade.setToValue(1);

        // Skip hint: fade in
        FadeTransition hintFade = new FadeTransition(Duration.millis(500), skipHint);
        hintFade.setFromValue(0);
        hintFade.setToValue(1);

        // Hold
        PauseTransition hold = new PauseTransition(Duration.millis(1600));

        // Fade out everything
        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), rootPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> navigate());

        // Assemble sequence
        fullSequence = new SequentialTransition(
            new PauseTransition(Duration.millis(200)),  // brief black pause
            iconsAnim,
            new PauseTransition(Duration.millis(120)),
            new ParallelTransition(titleAnim, hintFade),
            new PauseTransition(Duration.millis(200)),
            subtitleFade,
            hold,
            fadeOut
        );
        fullSequence.play();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void skip() {
        if (navigated) return;
        if (fullSequence != null) fullSequence.stop();
        // Quick fade out then navigate
        FadeTransition out = new FadeTransition(Duration.millis(300), rootPane);
        out.setFromValue(rootPane.getOpacity());
        out.setToValue(0);
        out.setOnFinished(e -> navigate());
        out.play();
    }

    private void navigate() {
        if (navigated) return;
        navigated = true;
        SportsManagerApp.navigateTo("SportSelectionView");
    }
}
