package com.example.javafx_helloworld;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

import java.io.File;


public class FXController {

    @FXML
    private TextArea filesTextArea;
    @FXML
    private TextFlow changesTextFlow;

    private String directoryPath;

    @FXML
    protected void handle_show_files_button() {
        handle_folder_selection();

        if(directoryPath == null || directoryPath.isEmpty()){
            return;
        }

        FileManager.set_initial_directory(directoryPath);
        FileManager.read_ignore_file();

        update_everything();
    }

    public void update_everything(){
        filesTextArea.clear();

        FileManager.find_all_files(directoryPath);

        FileManager.get_current_state_files()
                .forEach((filePath, v) -> filesTextArea.appendText(filePath + "\n"));

        if(FileManager.saved_state_exist()) {
            FileManager.detect_file_changes();
            changesTextFlow.getChildren().clear();
            show_changes();
        }
        FileManager.save_current_file_state();
    }

    public void recheck_files(){
        FileManager.empty_current_file_state_array();
        update_everything();
    }

    public void show_changes(){

        FileManager.get_current_changes_files()
                .forEach((state, arrayOfPaths) -> {
                    Color color ;
                    switch (state) {
                        case ADDED -> color = Color.GREEN;
                        case MODIFIED -> color = Color.YELLOW;
                        case DELETED -> color = Color.RED;
                        default -> color = Color.BLACK;
                    }
                    arrayOfPaths.forEach( file -> {
                        Text text = new Text(file + "\n");
                        text.setFill(color);
                        changesTextFlow.getChildren().add(text);
                    });
                });
    }

    protected void handle_folder_selection(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        directoryChooser.setInitialDirectory(new File("."));
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null)
            return;

        directoryPath = selectedDirectory.getAbsolutePath();

        HandleRepository.set_directory_path(directoryPath);

        if(HandleRepository.repository_doesnt_exist())
            HandleRepository.create_repository_Folder();

        if(HandleRepository.ignored_files_doesnt_exist())
            HandleRepository.create_ignore_file();

        FileManager.empty_current_file_state_array();
    }

}