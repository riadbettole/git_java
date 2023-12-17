package com.example.javafx_helloworld;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

public class FXController {

    @FXML
    private Button compareFilesButton;
    @FXML
    private Button recheckFilesButton;

    private TextFlow changesLinesTextFlow;

    private String directoryPath;

    @FXML
    private VBox dynamicCheckBoxContainer;
    CustomCheckBox[] customCheckBoxes;

    private void show_compare_button() {
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

    private void show_recheck_button() {
        recheckFilesButton.setVisible(true);
    }

    @FXML
    protected void open_project() {

        RepositoryManager.select_folder_of_project();
        directoryPath = RepositoryManager.get_directory_path();
        //RepositoryManager.set_directory_path(directoryPath);

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        show_recheck_button();
        show_compare_button();

        update_everything();
    }

    public void update_everything() {

        FileManager.find_all_files(directoryPath);
        System.out.println(RepositoryManager.PathOfStagingFile);
        if (StagingManager.saved_state_file_doesnt_exist()) {
            StagingManager.create_state_file();
        }
        show_file_changes();
    }

    @FXML
    public void recheck_files() {
        update_everything();
    }

    @FXML
    public void compare_files() {
        File f = new File(directoryPath + "/720_576.ps1");
        File f2 = new File(directoryPath + "/720_576_1.ps1");

        ComparatorManager.set_old_file_path(f.getPath());
        ComparatorManager.set_new_file_path(f2.getPath());

        ComparatorManager.to_map_files();

        ComparatorManager.compare();
    }

    public void show_file_changes() {
        Map<FileStateEnums, ArrayList<HashedFile>> changes = StagingManager.detect_staged_file_changes();
        dynamicCheckBoxContainer.getChildren().clear();

        int numberOfStrings = changes.values().stream().mapToInt(List::size).sum();

        customCheckBoxes = new CustomCheckBox[numberOfStrings];
        int index = 0;
        FileStateEnums[] orderStates = {FileStateEnums.ADDED, FileStateEnums.MODIFIED, FileStateEnums.DELETED, FileStateEnums.UNADDED};

        for (FileStateEnums state : orderStates) {
            if (!changes.containsKey(state)) continue;
            ArrayList<HashedFile> arrayOfPaths = changes.get(state);

            Text text = new Text();
            switch (state) {
                case ADDED -> text.setText("STAGED" + ": (select to unstage)\n");
                case DELETED -> text.setText("DELETED" + ": (select to restore)\n");
                case MODIFIED -> text.setText("MODIFIED" + ": (select to restore)\n");
                default -> text.setText("UNSTAGED" + ": (select to stage)\n");
            }

            dynamicCheckBoxContainer.getChildren().add(text);

            for (HashedFile hashedFile : arrayOfPaths) {

                customCheckBoxes[index] = new CustomCheckBox(hashedFile);

                dynamicCheckBoxContainer.getChildren().add(customCheckBoxes[index]);
                index++;
            }
        }


        Button executeButton = new Button("Execute"); //check if you do it a second time what will happen will it have two buttons or
        executeButton.setOnAction(e -> handle_stanging());

        dynamicCheckBoxContainer.getChildren().add(executeButton);
    }

    public void handle_stanging() {

        for (CustomCheckBox customCheckBox : customCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_hashed_file();
                switch (hashedFile.get_state()) {
                    case UNADDED -> StagingManager.add_file_to_staging(hashedFile);
                    case ADDED -> StagingManager.delete_file_from_staging(hashedFile);
                    case DELETED -> StagingManager.restore_file_from_staging(hashedFile);
//                    case MODIFIED ->
                    default -> StagingManager.restore_file_from_staging(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        recheck_files();
    }

    public void show_line_changes() {

        changesLinesTextFlow = new TextFlow();
        ArrayList<LineChanges> lineChangesList = ComparatorManager.get_current_changes_lines();
        Collections.sort(lineChangesList);
        lineChangesList
                .forEach((changes) -> {
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

}

