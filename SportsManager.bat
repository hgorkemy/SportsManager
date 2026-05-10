@echo off
javaw --module-path "%~dp0lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar "%~dp0target\sports-manager-1.0-SNAPSHOT-fat.jar"
