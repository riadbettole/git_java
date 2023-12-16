package com.example.javafx_helloworld;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FXController {

//    @FXML
//    private TextArea filesTextArea;
    @FXML
    private TextFlow unstagedTextFlow;
    @FXML
    private Button compareFilesButton;

    private TextFlow changesLinesTextFlow;

    private String directoryPath;

    @FXML
    private VBox dynamicCheckBoxContainer;
    CheckBox[] checkboxes;
    List<String> allCheckedFilesForStaging = new ArrayList<>();

    private void handle_compare_button(){
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
    }

    @FXML
    protected void handle_show_files_button() {
        handle_folder_selection();

        handle_compare_button();

        if(directoryPath == null || directoryPath.isEmpty()){
            return;
        }

        StateManager.set_initial_directory(directoryPath);
        StateManager.read_ignore_file();
        StagingManager.set_initial_path_Of_zipped_folders(directoryPath);

        update_everything();
    }

    public void update_everything(){
        //filesTextArea.clear();

        StateManager.find_all_files(directoryPath);

//        Map<String, Long> currentStateOfFile = StateManager.get_current_state_files();
//        currentStateOfFile.forEach((filePath, v) -> filesTextArea.appendText(filePath + "\n"));

        if(StateManager.saved_state_file_exist()) {
            StateManager.detect_file_changes();
            unstagedTextFlow.getChildren().clear();
            show_file_changes();
        }
        StateManager.save_current_file_state();
    }

    @FXML
    public void recheck_files(){
        StateManager.empty_state_staging_maps();
        update_everything();
    }

    @FXML
    public void compare_files(){
        File f = new File(directoryPath+"/720_576.ps1");
        File f2 = new File(directoryPath+"/720_576_1.ps1");

        ComparatorManager.set_old_file_path(f.getPath());
        ComparatorManager.set_new_file_path(f2.getPath());

        ComparatorManager.to_map_files();

        ComparatorManager.compare();
    }

    public void show_file_changes(){
        Map<FileStateEnums, ArrayList<String>> changes = StateManager.get_current_changes_files();
        dynamicCheckBoxContainer.getChildren().clear();
        int numberOfStrings = changes.values().stream().mapToInt(List::size).sum();
        checkboxes = new CheckBox[numberOfStrings];

        int currentIndex = 0;

        for (Map.Entry<FileStateEnums, ArrayList<String>> entry : changes.entrySet()) {
            FileStateEnums state = entry.getKey();
            ArrayList<String> arrayOfPaths = entry.getValue();

            for (String arrayOfPath : arrayOfPaths) {
                checkboxes[currentIndex] = new CheckBox(arrayOfPath);
                if(state == FileStateEnums.ADDED)
                    checkboxes[currentIndex].setStyle("-fx-text-fill: green;");
                else if(state == FileStateEnums.UNADDED)
                    checkboxes[currentIndex].setStyle("-fx-text-fill: red;");

                dynamicCheckBoxContainer.getChildren().add(checkboxes[currentIndex]);
                currentIndex++;
            }
        }


        Button executeButton = new Button("Execute"); //check if you do it a second time what will happen will it have two buttons or
        executeButton.setOnAction(e -> handle_stanging());

        dynamicCheckBoxContainer.getChildren().add(executeButton);
    }

    public void handle_stanging(){

        for (CheckBox checkBox : checkboxes) {
            if (checkBox.isSelected()) {
                // Perform your operation on the selected path
                allCheckedFilesForStaging.add(checkBox.getText());
                System.out.println(checkBox.getText());
            }
        }
        StagingManager.add_file_to_staging((ArrayList<String>) allCheckedFilesForStaging);
        recheck_files();
    }

    public void show_line_changes(){

        changesLinesTextFlow = new TextFlow();
        ArrayList<LineChanges> lineChangesList = ComparatorManager.get_current_changes_lines();
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

        if(HandleRepository.zipped_folder_doesnt_exist())
            HandleRepository.create_folder_of_zipped_folders();

        StateManager.empty_state_staging_maps();

    }

}