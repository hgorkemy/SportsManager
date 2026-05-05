package com.sportsmanager;

import com.sportsmanager.core.factory.SportRegistry;
import com.sportsmanager.football.FootballFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application entry point.
 * Run with: mvn javafx:run
 *
 * Implemented by: Irmak Önder
 */
public class SportsManagerApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Sports Manager");
        stage.setResizable(true);
        stage.setMaximized(true);
        navigateTo("SportSelectionView");
        stage.show();
    }

    /**
     * Navigates to the given FXML view.
     * All controllers use this to switch screens.
     */
    public static void navigateTo(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SportsManagerApp.class.getResource("/com/sportsmanager/ui/" + viewName + ".fxml")
            );
            javafx.scene.Parent root = loader.load();
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                    SportsManagerApp.class.getResource("/com/sportsmanager/ui/styles.css").toExternalForm()
                );
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }
        } catch (Exception e) {
            System.err.println("Could not load view: " + viewName);
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        SportRegistry.register("Football", new FootballFactory());
        launch(args);
    }
}
