package com.example.javafx_helloworld.controllers;

import com.example.javafx_helloworld.enums.FileStateEnums;
import com.example.javafx_helloworld.models.CustomCheckBox;
import com.example.javafx_helloworld.models.CustomStack;
import com.example.javafx_helloworld.models.HashedFile;
import com.example.javafx_helloworld.models.LineChanges;
import com.example.javafx_helloworld.utils.ComparatorManager;
import com.example.javafx_helloworld.utils.FileManager;
import com.example.javafx_helloworld.utils.RepositoryManager;
import com.example.javafx_helloworld.utils.StagingManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FXController {

    @FXML
    private Button commitButton;
    @FXML
    private Button compareFilesButton;
    @FXML
    private Button deleteRepoButton;
    @FXML
    private Button recheckFilesButton;

    private TextFlow changesLinesTextFlow;

    private String directoryPath;

    @FXML
    private VBox dynamicCheckBoxContainer;
    @FXML
    private VBox recentProjectsContainer;

    @FXML
    private HBox executeButtonContainer;

    CustomStack<String> recentProjects;

    CustomCheckBox[] customCheckBoxes;

    Button executeButton = new Button("Stage/ Unstage");
    Button restoreButton = new Button("Restore");
    Button restageButton = new Button("Restage");

    @FXML
    protected void open_project() {

        RepositoryManager.select_folder_of_project();
        directoryPath = RepositoryManager.get_directory_path();
        recentProjects.push(directoryPath);
        update_recent_project_order();
        //RepositoryManager.set_directory_path(directoryPath);

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        show_buttons();

        show_recent_projects();
        update_everything();
    }

    protected void open_project_recent_project(String _directoryPath) {

        directoryPath = _directoryPath;
        RepositoryManager.set_directory_path(directoryPath);
        RepositoryManager.files_and_folders_exist();

        recentProjects.push(directoryPath);
        update_recent_project_order();
        //RepositoryManager.set_directory_path(directoryPath);

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        show_buttons();

        show_recent_projects();
        update_everything();
    }

    public void update_everything() {

        FileManager.clear_current_all_files_present();
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

        if(numberOfStrings == 0){
            Text text = new Text("Empty Folder add something!");
            dynamicCheckBoxContainer.getChildren().add(text);
            return;
        }

        customCheckBoxes = new CustomCheckBox[numberOfStrings];
        int index = 0;
        FileStateEnums[] orderStates = {FileStateEnums.ADDED, FileStateEnums.MODIFIED, FileStateEnums.DELETED, FileStateEnums.UNADDED};

        executeButton.setOnAction(e -> handle_stanging());
        restoreButton.setOnAction(e -> handle_restore());
        restageButton.setOnAction(e -> handle_restage());



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

                customCheckBoxes[index].setOnAction(event -> check_button_states(customCheckBoxes));

                dynamicCheckBoxContainer.getChildren().add(customCheckBoxes[index]);
                index++;
            }
        }

        executeButtonContainer.getChildren().clear();

        executeButtonContainer.getChildren().add(executeButton);
        executeButtonContainer.getChildren().add(restoreButton);
        executeButtonContainer.getChildren().add(restageButton);

        dynamicCheckBoxContainer.getChildren().add(executeButtonContainer);


    }

    private void check_button_states(CustomCheckBox[] checkBoxes) {
        Set<FileStateEnums> checked = new HashSet<>();
        for (CustomCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                checked.add(checkBox.get_hashed_file().get_state());
            }
        }
        if (checked.isEmpty()) {
            restoreButton.setDisable(false);
            restageButton.setDisable(false);
        }
        checked.forEach(k -> {
            if (checked.contains(FileStateEnums.ADDED) || checked.contains(FileStateEnums.UNADDED)) {
                restoreButton.setDisable(true);
                restageButton.setDisable(true);
            } else if (checked.contains(FileStateEnums.DELETED)) {
                restoreButton.setDisable(false);
                restageButton.setDisable(true);
            }
        });
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
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheck_files();
    }

    public void handle_restore() {

        for (CustomCheckBox customCheckBox : customCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_hashed_file();
                switch (hashedFile.get_state()) {
                    case DELETED, MODIFIED -> StagingManager.restore_file_from_staging(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheck_files();
    }

    public void handle_restage() {

        for (CustomCheckBox customCheckBox : customCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_hashed_file();
                if (hashedFile.get_state() == FileStateEnums.MODIFIED) {
                    StagingManager.add_file_to_staging(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
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

                    String currentLine = changes.getLine();
                    Text text = new Text(currentLine + "\n");
                    System.out.println(currentLine);
                    text.setFill(color);
                    changesLinesTextFlow.getChildren().add(text);
                });
    }

    public void show_recent_projects(){
        load_recent_project_order();
        recentProjectsContainer.getChildren().clear();

        CustomStack<String> reverseString = new CustomStack<>(recentProjects);

        Collections.reverse(reverseString);

        for(String path: reverseString){
            Hyperlink hyperlink = new Hyperlink(path);
            hyperlink.setOnAction(event -> {
                String hyperlinkText = hyperlink.getText();
                open_project_recent_project(hyperlinkText);
            });
            recentProjectsContainer.getChildren().add(hyperlink);
        }
    }

    public void update_recent_project_order(){
        String userHome = System.getProperty("user.home");
        String relativePath = "Documents";
        Path documentsPath = Paths.get(userHome, relativePath);
        Path filePath = documentsPath.resolve("recent_folders_git");
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toString()))){
            oos.writeObject(recentProjects);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void load_recent_project_order(){
        String userHome = System.getProperty("user.home");
        String relativePath = "Documents";
        Path documentsPath = Paths.get(userHome, relativePath);
        Path filePath = documentsPath.resolve("recent_folders_git");
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))){
            recentProjects = (CustomStack<String>)ois.readObject();
        }catch(IOException| ClassNotFoundException e){
            recentProjects = new CustomStack<>();
        }
    }

    private void show_buttons(){
        show_recheck_button();
        show_commit_button();
        show_compare_button();
        show_delete_repo_button();
    }

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

    private void show_commit_button() {
        commitButton.setVisible(true);
    }

    private void show_delete_repo_button() {
        deleteRepoButton.setVisible(true);
        deleteRepoButton.setOnAction(event->{
            Stage warningStage = new Stage();
            warningStage.setTitle("WARNING DELETE");
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            Text text = new Text("Do you really want to delete this repo?");
            Button button = new Button("Confirm");
            button.setOnAction(e->RepositoryManager.delete_this_repo());
            vbox.getChildren().add(text);
            vbox.getChildren().add(button);
            Scene warningScene = new Scene(vbox,300,200);
            warningStage.setScene(warningScene);
            warningStage.show();
        });
    }

    private void show_recheck_button() {
        recheckFilesButton.setVisible(true);
    }
}

