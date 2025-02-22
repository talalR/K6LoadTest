package com.loadtest;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptService {
    private final String DOWNLOADS_PATH = System.getProperty("user.home") + File.separator + "Downloads";
    private final String K6_PATH = "k6";

    public String generateScript(String curlCommand, String testName, String duration, String vus, List<String> thresholds) throws Exception {
        // Parse cURL command
        String url = extractUrl(curlCommand);
        String method = extractMethod(curlCommand);
        List<String> headers = extractHeaders(curlCommand);
        String body = extractBody(curlCommand);

        if (url.isEmpty()) {
            throw new Exception("Failed to extract URL from cURL command.");
        }

        // Build script
        StringBuilder script = new StringBuilder();
        script.append("import http from 'k6/http';\n");
        script.append("import { check, sleep } from 'k6';\n");
        script.append("import { Trend, Rate, Counter } from 'k6/metrics';\n");
        script.append("import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';\n\n");

        // Add custom metrics
        script.append("// Custom metrics for detailed analysis\n");
        script.append("const responseTimes = new Trend('response_times');\n");
        script.append("const requestRate = new Rate('request_rate');\n");
        script.append("const successRate = new Rate('success_rate');\n");
        script.append("const errorCounter = new Counter('error_counter');\n\n");

        // Add thresholds
        script.append("// Thresholds for pass/fail criteria\n");
        script.append("export let thresholds = {\n");
        if (thresholds.isEmpty()) {
            script.append("  'http_req_duration': ['p(95)<30000'],\n");
            script.append("  'success_rate': ['rate>0.95'],\n");
        } else {
            for (String threshold : thresholds) {
                String[] parts = threshold.split(": ");
                if (parts.length == 2) {
                    script.append("  '").append(parts[0]).append("': ['").append(parts[1]).append("'],\n");
                }
            }
        }
        script.append("};\n\n");

        // Add options
        script.append("export let options = {\n");
        script.append("  vus: ").append(vus).append(",\n");
        script.append("  duration: '").append(duration).append("',\n");
        script.append("  thresholds,\n");
        script.append("  summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],\n");
        script.append("};\n\n");

        // Add default function
        script.append("export default function () {\n");
        script.append("  let url = '").append(url).append("';\n");
        script.append("  let params = {\n");

        // Add headers
        if (!headers.isEmpty()) {
            script.append("    headers: {\n");
            for (String header : headers) {
                String[] headerParts = header.split(": ", 2);
                if (headerParts.length == 2) {
                    script.append("      '").append(headerParts[0]).append("': '")
                            .append(headerParts[1].replace("'", "\\'")).append("',\n");
                }
            }
            script.append("    },\n");
        }

        // Add body if it exists
        if (body != null && !body.isEmpty()) {
            script.append("    body: '").append(body.replace("'", "\\'")).append("',\n");
        }

        script.append("  };\n\n");

        // Add request based on method
        script.append("  let response = http.").append(method.toLowerCase()).append("(url, params);\n\n");

        // Add tracking and checks
        script.append("  // Track request count\n");
        script.append("  requestRate.add(1);\n\n");

        script.append("  // Track response times\n");
        script.append("  const duration = response.timings.duration;\n");
        script.append("  responseTimes.add(duration);\n\n");

        script.append("  // Check if request was successful\n");
        script.append("  const isSuccess = response.status >= 200 && response.status < 300;\n");
        script.append("  successRate.add(isSuccess);\n\n");

        script.append("  if (!isSuccess) {\n");
        script.append("    errorCounter.add(1);\n");
        script.append("    console.log(`Error: ${response.status} - ${response.body.substring(0, 100)}...`);\n");
        script.append("  }\n\n");

        script.append("  // Run checks\n");
        script.append("  check(response, {\n");
        script.append("    'is status 200': (r) => r.status === 200,\n");
        script.append("    'response time < 500ms': (r) => r.timings.duration < 500,\n");
        script.append("    'response time < 1000ms': (r) => r.timings.duration < 1000,\n");
        script.append("  });\n\n");

        script.append("  sleep(1);\n");
        script.append("}\n\n");

        // Add handleSummary function to generate HTML report in the project directory
        script.append("export function handleSummary(data) {\n");
        script.append("  console.log('Generating HTML report...');\n"); // Debug log
        script.append("  if (!data) {\n");
        script.append("    console.error('No data received for summary.');\n"); // Debug log
        script.append("    return {};\n");
        script.append("  }\n");
        script.append("  return {\n");
        script.append("    './").append(testName).append("_report.html': htmlReport(data),\n"); // Save in project directory
        script.append("  };\n");
        script.append("}\n");

        return script.toString();
    }    private String extractUrl(String curlCommand) {
        Pattern urlPattern = Pattern.compile("(?:curl|\\s+)['\"](https?://[^'\"]+)['\"]");
        Matcher urlMatcher = urlPattern.matcher(curlCommand);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        } else {
            urlPattern = Pattern.compile("(?:curl|\\s+)(https?://\\S+)");
            urlMatcher = urlPattern.matcher(curlCommand);
            if (urlMatcher.find()) {
                String url = urlMatcher.group(1);
                if (url.contains(" ")) {
                    url = url.substring(0, url.indexOf(" "));
                }
                return url;
            }
        }
        return "";
    }

    private String extractMethod(String curlCommand) {
        Pattern methodPattern = Pattern.compile("-X\\s+([A-Z]+)|--request\\s+([A-Z]+)");
        Matcher methodMatcher = methodPattern.matcher(curlCommand);
        if (methodMatcher.find()) {
            return methodMatcher.group(1) != null ? methodMatcher.group(1) : methodMatcher.group(2);
        }
        return "GET"; // Default to GET if no method is specified
    }

    private List<String> extractHeaders(String curlCommand) {
        List<String> headers = new ArrayList<>();
        Pattern headerPattern = Pattern.compile("-H\\s+['\"]([^'\"]+)['\"]|--header\\s+['\"]([^'\"]+)['\"]");
        Matcher headerMatcher = headerPattern.matcher(curlCommand);
        while (headerMatcher.find()) {
            String header = headerMatcher.group(1) != null ? headerMatcher.group(1) : headerMatcher.group(2);
            headers.add(header);
        }
        return headers;
    }

    private String extractBody(String curlCommand) {
        Pattern bodyPattern = Pattern.compile("-d\\s+['\"]([^'\"]+)['\"]|--data\\s+['\"]([^'\"]+)['\"]|--data-raw\\s+['\"]([^'\"]+)['\"]");
        Matcher bodyMatcher = bodyPattern.matcher(curlCommand);
        if (bodyMatcher.find()) {
            return bodyMatcher.group(1) != null ? bodyMatcher.group(1) :
                    (bodyMatcher.group(2) != null ? bodyMatcher.group(2) : bodyMatcher.group(3));
        }
        return null;
    }

    public void saveScript(String scriptContent, String testName, Label statusLabel) {
        try {
            if (scriptContent.isEmpty()) {
                throw new IOException("No script to save.");
            }

            File downloadsDir = new File(DOWNLOADS_PATH);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            String filename = testName.replaceAll("[^a-zA-Z0-9-_]", "_") + ".js";
            String fullPath = DOWNLOADS_PATH + File.separator + filename;

            FileWriter writer = new FileWriter(fullPath);
            writer.write(scriptContent);
            writer.close();

            statusLabel.setText("Script saved as " + fullPath);
        } catch (IOException e) {
            statusLabel.setText("Failed to save script: " + e.getMessage());
        }
    }

    public void runTest(String scriptContent, String testName, ProgressBar progressBar, Label statusLabel) {
        try {
            if (scriptContent.isEmpty()) {
                throw new IOException("No script to run.");
            }

            // Create downloads directory if it doesn't exist
            File downloadsDir = new File(DOWNLOADS_PATH);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Save the script to a file
            String filename = testName.replaceAll("[^a-zA-Z0-9-_]", "_") + ".js";
            String fullPath = DOWNLOADS_PATH + File.separator + filename;

            FileWriter writer = new FileWriter(fullPath);
            writer.write(scriptContent);
            writer.close();

            // Log that the script has been saved
            System.out.println("Script saved to: " + fullPath);

            // Update the UI to indicate the test is starting
            javafx.application.Platform.runLater(() -> {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS); // Show indeterminate progress
                statusLabel.setText("Test is running...");
            });

            // Run k6 in a separate thread
            new Thread(() -> {
                try {
                    // Log that the test is starting
                    System.out.println("Starting k6 test...");

                    // Build the process to run k6
                    ProcessBuilder pb = new ProcessBuilder(K6_PATH, "run", fullPath);
                    pb.redirectErrorStream(true); // Combine stdout and stderr
                    Process process = pb.start();

                    // Log the command being executed
                    System.out.println("Executing command: " + String.join(" ", pb.command()));

                    // Read the output of the process
                    StringBuilder output = new StringBuilder();
                    try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream())) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            output.append(line).append("\n");
                            System.out.println(line); // Print each line to the console
                        }
                    }

                    // Wait for the process to complete
                    int exitCode = process.waitFor();
                    System.out.println("k6 process exited with code: " + exitCode);

                    // Update the UI on the JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(1.0); // Set progress to 100% when done
                        if (exitCode == 0) {
                            statusLabel.setText("Test completed successfully. Reports saved to downloads.");
                        } else {
                            statusLabel.setText("Test completed with issues (exit code: " + exitCode + "). Reports saved to downloads.");
                        }

                        // Show test results in a new window
                        TextArea resultArea = new TextArea(output.toString());
                        resultArea.setEditable(false);
                        resultArea.setWrapText(true);
                        resultArea.setPrefWidth(800);
                        resultArea.setPrefHeight(600);

                        Stage resultStage = new Stage();
                        resultStage.setTitle("Test Results");
                        resultStage.setScene(new Scene(new BorderPane(resultArea), 800, 600));
                        resultStage.show();
                    });
                } catch (Exception e) {
                    // Log any errors that occur during the test execution
                    System.err.println("Error running k6 test: " + e.getMessage());
                    e.printStackTrace();

                    // Update the UI on the JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(0); // Reset progress on failure
                        statusLabel.setText("Test failed: " + e.getMessage());
                    });
                }
            }).start();
        } catch (IOException e) {
            // Log any errors that occur while saving the script
            System.err.println("Error saving script: " + e.getMessage());
            e.printStackTrace();

            // Update the UI on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                progressBar.setProgress(0); // Reset progress on failure
                statusLabel.setText("Failed to run test: " + e.getMessage());
            });
        }
    }



}