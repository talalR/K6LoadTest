K6 Load Test Generator
This project is a JavaFX-based application for generating and running load tests using the k6 tool. It allows users to input cURL commands, configure test parameters, and generate k6 scripts.

Prerequisites
Before running the application, ensure you have the following installed:

Java Development Kit (JDK) 17:

Download and install JDK 17 from Oracle JDK or OpenJDK.

Verify the installation:
java -version
The output should show java version "17.x.x".

JavaFX SDK 17:

Download the JavaFX SDK from Gluon.

Extract the SDK to a directory of your choice (e.g., C:\javafx-sdk-17.0.14).



Running the Application
Step 1: Clone the Repository
Clone this repository to your local machine:
git clone https://github.com/your-username/K6UILoadTest.git
cd K6UILoadTest


Step 2: Build the Project
Build the project using Maven:
mvn clean package

This will generate a JAR file in the target directory (K6UILoadTest-1.0-SNAPSHOT.jar).

Step 3: Run the Application
Run the application using the following command. Replace D:\k6\javafx-sdk-17.0.14 with the path where you extracted the JavaFX SDK.

java --module-path /path/to/javafx-sdk-17.0.14/lib --add-modules javafx.controls,javafx.fxml -jar target/K6UILoadTest-1.0-SNAPSHOT.jar


Example:
If you extracted the JavaFX SDK to C:\javafx-sdk-17.0.14, the command would be:
java --module-path C:\javafx-sdk-17.0.14\lib --add-modules javafx.controls,javafx.fxml -jar target/K6UILoadTest-1.0-SNAPSHOT.jar
