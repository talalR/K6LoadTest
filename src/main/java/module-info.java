module com.loadtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens com.loadtest to javafx.fxml, javafx.graphics;
    exports com.loadtest;
}