module com.example.javafx_helloworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;
    requires org.apache.httpcomponents.httpcore;


    opens com.example.javafx_helloworld to javafx.fxml;
    exports com.example.javafx_helloworld;
    exports com.example.javafx_helloworld.controllers;
    opens com.example.javafx_helloworld.controllers to javafx.fxml;
    exports com.example.javafx_helloworld.enums;
    opens com.example.javafx_helloworld.enums to javafx.fxml;
    exports com.example.javafx_helloworld.models;
    opens com.example.javafx_helloworld.models to javafx.fxml;
    exports com.example.javafx_helloworld.utils;
    opens com.example.javafx_helloworld.utils to javafx.fxml;
}