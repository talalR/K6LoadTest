K6 Load Test Generator

A JavaFX-based application for generating and running load tests using the k6 tool. This application allows users to:

Input cURL commands

Configure test parameters

Generate k6 scripts

🛠 Prerequisites

Before running the application, ensure you have the following installed:

✅ Java Development Kit (JDK) 17

Download and install JDK 17 from Oracle JDK or OpenJDK.

Verify the installation:

java -version

Expected output:

java version "17.x.x"

✅ JavaFX SDK 17

Download the JavaFX SDK from Gluon.

Extract the SDK to a directory of your choice, e.g., C:\javafx-sdk-17.0.14.

🚀 Running the Application

Step 1: Clone the Repository

git clone https://github.com/your-username/K6UILoadTest.git
cd K6UILoadTest

Step 2: Build the Project

Use Maven to build the project:

mvn clean package

This will generate a JAR file in the target directory:

target/K6UILoadTest-1.0-SNAPSHOT.jar

Step 3: Run the Application

Run the application using the following command:

java --module-path /path/to/javafx-sdk-17.0.14/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/K6UILoadTest-1.0-SNAPSHOT.jar

🔹 Example (Windows):

java --module-path C:\javafx-sdk-17.0.14\lib --add-modules javafx.controls,javafx.fxml -jar target/K6UILoadTest-1.0-SNAPSHOT.jar

🔹 Example (Linux/Mac):

java --module-path /opt/javafx-sdk-17.0.14/lib --add-modules javafx.controls,javafx.fxml -jar target/K6UILoadTest-1.0-SNAPSHOT.jar

📜 License

This project is licensed under the MIT License.

