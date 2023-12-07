module com.example.javafx_helloworld {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.javafx_helloworld to javafx.fxml;
    exports com.example.javafx_helloworld;
}