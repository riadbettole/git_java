package com.example.javafx_helloworld;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class FXController {

    @FXML
    private TextArea filesTextArea;
    @FXML
    private TextFlow changesTextFlow;
    @FXML
    private Button compareFilesButton;

    private TextFlow changesLinesTextFlow;

    private String directoryPath;

    @FXML
    protected void handle_show_files_button() {
        handle_folder_selection();

        compareFilesButton.setVisible(true);
        compareFilesButton.setOnAction(event -> {
            compare_files();
            show_line_changes();
            Stage resultStage = new Stage();
            resultStage.setTitle("Result Window");
            StackPane resultLayout = new StackPane();
            resultLayout.getChildren().add(changesLinesTextFlow); // Display the result (replace with appropriate UI)
            Scene resultScene = new Scene(resultLayout, 300, 200);
            resultStage.setScene(resultScene);
            resultStage.show();
        });

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
            show_file_changes();
        }
        FileManager.save_current_file_state();
    }

    @FXML
    public void recheck_files(){
        FileManager.empty_current_file_state_array();
        update_everything();
    }

    @FXML
    public void compare_files(){
        File f = new File(directoryPath+"/720_576.ps1");
        File f2 = new File(directoryPath+"/720_576_1.ps1");

        FileComparator.set_old_file_path(f.getPath());
        FileComparator.set_new_file_path(f2.getPath());

        FileComparator.to_map_files();

        FileComparator.compare();
    }

    public void show_file_changes(){

        FileManager.get_current_changes_files()
                .forEach((state, arrayOfPaths) -> {
                    Color color ;
                    switch (state) {
                        case ADDED -> color = Color.GREEN;
                        case MODIFIED -> color = Color.ORANGE;
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

    public void show_line_changes(){

        changesLinesTextFlow = new TextFlow();
        ArrayList<LineChanges> lineChangesList = FileComparator.get_current_changes_lines();
        Collections.sort(lineChangesList);
        lineChangesList
                .forEach((changes)->{
                    Color color;
                    switch (changes.getColor()) {
                        case ADDED -> color = Color.GREEN;
                        case DELETED -> color = Color.RED;
                        default -> color = Color.BLACK;
                    }

                    String currentLine = changes.line;
                    Text text = new Text(currentLine + "\n");
                    System.out.println(currentLine);
                    text.setFill(color);
                    changesLinesTextFlow.getChildren().add(text);
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
            HandleRepository.create_repository_folder();

        if(HandleRepository.ignored_files_doesnt_exist())
            HandleRepository.create_ignore_file();

        FileManager.empty_current_file_state_array();

    }

}