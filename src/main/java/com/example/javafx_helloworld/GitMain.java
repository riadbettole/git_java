package com.example.javafx_helloworld;

import com.example.javafx_helloworld.controllers.FXController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;


public class GitMain extends Application {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GitMain.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);

        stage.setTitle("GIT JAVA!");
        stage.setScene(scene);

        stage.show();

        FXController controller = fxmlLoader.getController();
        controller.showRecentProjects();
    }

    public static void main(String[] args) {
        launch();
    }
}
