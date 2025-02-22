package com.loadtest;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class LoadTestGenerator extends Application {
    private UIBuilder uiBuilder;
    private ScriptService scriptService;

    @Override
    public void start(Stage primaryStage) {
        uiBuilder = new UIBuilder();
        scriptService = new ScriptService();

        // Setup UI
        BorderPane root = uiBuilder.createMainLayout();
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dynamic Load Test Generator");
        primaryStage.show();

        // Bind actions
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Generate Script Button
        uiBuilder.getGenerateButton().setOnAction(e -> {
            String curl = uiBuilder.getCurlCommandArea().getText();
            String testName = uiBuilder.getTestNameField().getText();
            String duration = uiBuilder.getDurationField().getText();
            String vus = uiBuilder.getVirtualUsersField().getText();
            List<String> thresholds = uiBuilder.getThresholdListView().getItems();

            try {
                String script = scriptService.generateScript(curl, testName, duration, vus, thresholds);
                uiBuilder.getGeneratedScriptArea().setText(script);
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        // Save Script Button
        uiBuilder.getSaveButton().setOnAction(e -> {
            String script = uiBuilder.getGeneratedScriptArea().getText();
            String testName = uiBuilder.getTestNameField().getText();
            scriptService.saveScript(script, testName, uiBuilder.getStatusLabel());
        });

        // Run Test Button
        uiBuilder.getRunButton().setOnAction(e -> {
            String script = uiBuilder.getGeneratedScriptArea().getText();
            String testName = uiBuilder.getTestNameField().getText();
            scriptService.runTest(
                    script,
                    testName,
                    uiBuilder.getProgressBar(),
                    uiBuilder.getStatusLabel()
            );
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}