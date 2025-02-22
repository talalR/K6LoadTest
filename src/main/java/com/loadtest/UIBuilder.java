package com.loadtest;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UIBuilder {
    private TextArea curlCommandArea, generatedScriptArea;
    private TextField durationField, virtualUsersField, testNameField;
    private ProgressBar progressBar;
    private Label statusLabel;
    private ListView<String> thresholdListView;
    private Button generateButton, saveButton, runButton;

    public BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Create left panel (input)
        VBox leftPanel = createInputPanel();

        // Create right panel (output)
        VBox rightPanel = createOutputPanel();

        // Create bottom panel (execution)
        HBox bottomPanel = createExecutionPanel();

        // Add panels to main layout
        mainLayout.setLeft(leftPanel);
        mainLayout.setRight(rightPanel);
        mainLayout.setBottom(bottomPanel);

        return mainLayout;
    }

    private VBox createInputPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(600);

        Label curlLabel = new Label("Enter cURL Command:");
        curlCommandArea = new TextArea();
        curlCommandArea.setPrefHeight(200);
        curlCommandArea.setWrapText(true);
        curlCommandArea.setPromptText("Paste your cURL command here...");

        // Test configuration
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);

        Label testNameLabel = new Label("Test Name:");
        testNameField = new TextField("api-load-test");

        Label durationLabel = new Label("Duration (e.g., 1m, 30s):");
        durationField = new TextField("1m");

        Label vusLabel = new Label("Virtual Users:");
        virtualUsersField = new TextField("50");

        configGrid.add(testNameLabel, 0, 0);
        configGrid.add(testNameField, 1, 0);
        configGrid.add(durationLabel, 0, 1);
        configGrid.add(durationField, 1, 1);
        configGrid.add(vusLabel, 0, 2);
        configGrid.add(virtualUsersField, 1, 2);

        // Thresholds section
        Label thresholdsLabel = new Label("Performance Thresholds:");
        HBox thresholdInputBox = new HBox(10);
        ComboBox<String> thresholdTypeComboBox = new ComboBox<>();
        thresholdTypeComboBox.getItems().addAll(
                "http_req_duration", "http_req_failed", "http_reqs", "http_req_waiting", "http_req_connecting"
        );
        thresholdTypeComboBox.setValue("http_req_duration");

        TextField thresholdValueField = new TextField("p(95)<5000");
        thresholdValueField.setPromptText("e.g. p(95)<5000");

        Button addThresholdButton = new Button("Add");
        addThresholdButton.setOnAction(e -> {
            String type = thresholdTypeComboBox.getValue();
            String value = thresholdValueField.getText().trim();
            if (!value.isEmpty()) {
                thresholdListView.getItems().add(type + ": " + value);
                thresholdValueField.clear();
            }
        });

        thresholdInputBox.getChildren().addAll(thresholdTypeComboBox, thresholdValueField, addThresholdButton);

        thresholdListView = new ListView<>();
        thresholdListView.setPrefHeight(100);

        Button removeThresholdButton = new Button("Remove Selected");
        removeThresholdButton.setOnAction(e -> {
            int selectedIdx = thresholdListView.getSelectionModel().getSelectedIndex();
            if (selectedIdx >= 0) {
                thresholdListView.getItems().remove(selectedIdx);
            }
        });

        // Button to generate script
        generateButton = new Button("Generate k6 Script");
        generateButton.setPrefWidth(200);

        panel.getChildren().addAll(
                curlLabel, curlCommandArea,
                new Separator(),
                new Label("Test Configuration:"),
                configGrid,
                new Separator(),
                thresholdsLabel,
                thresholdInputBox,
                thresholdListView,
                removeThresholdButton,
                new Separator(),
                generateButton
        );

        return panel;
    }

    private VBox createOutputPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(600);

        Label scriptLabel = new Label("Generated k6 Script:");
        generatedScriptArea = new TextArea();
        generatedScriptArea.setPrefHeight(500);
        generatedScriptArea.setEditable(true);
        generatedScriptArea.setWrapText(true);

        saveButton = new Button("Save Script");
        saveButton.setPrefWidth(150);

        panel.getChildren().addAll(scriptLabel, generatedScriptArea, saveButton);
        return panel;
    }

    private HBox createExecutionPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));

        runButton = new Button("Run Test");
        runButton.setPrefWidth(150);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        statusLabel = new Label("Ready");

        panel.getChildren().addAll(runButton, progressBar, statusLabel);
        return panel;
    }

    // Getters for UI components
    public TextArea getCurlCommandArea() { return curlCommandArea; }
    public TextArea getGeneratedScriptArea() { return generatedScriptArea; }
    public TextField getTestNameField() { return testNameField; }
    public TextField getDurationField() { return durationField; }
    public TextField getVirtualUsersField() { return virtualUsersField; }
    public ListView<String> getThresholdListView() { return thresholdListView; }
    public Button getGenerateButton() { return generateButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getRunButton() { return runButton; }
    public ProgressBar getProgressBar() { return progressBar; }
    public Label getStatusLabel() { return statusLabel; }
}