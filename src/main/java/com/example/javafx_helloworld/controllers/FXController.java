package com.example.javafx_helloworld.controllers;

import com.example.javafx_helloworld.enums.FileStateEnums;
import com.example.javafx_helloworld.models.*;
import com.example.javafx_helloworld.utils.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FXController {

    @FXML private Button checkoutButton;
    @FXML private Button commitButton;
    @FXML private Button compareFilesButton;
    @FXML private Button deleteRepoButton;
    @FXML private Button recheckFilesButton;
    @FXML private Button openFolderButton;
    @FXML private Button branchWindowButton;

    @FXML private Text textNameProject;
    @FXML private Text currentBranch;
    private String directoryPath;

    @FXML private VBox currentFilesCheckBoxContainer;
    @FXML private VBox recentProjectsContainer;
    @FXML private HBox executeButtonContainer;

    CustomStack<String> recentProjects;

    ArrayList<CustomCheckBox<HashedFile>> customHashedFilesCheckBoxes;

    Button stageUnstageButton;
    Button restoreButton;
    Button restageButton;

    private void show_buttons() {
        compareFilesButton.setVisible(true);
        commitButton      .setVisible(true);
        deleteRepoButton  .setVisible(true);
        recheckFilesButton.setVisible(true);
        openFolderButton  .setVisible(true);
        checkoutButton    .setVisible(true);
        branchWindowButton.setVisible(true);
    }
    @FXML private void open_project() {
        RepositoryManager.select_folder_of_project();
        directoryPath = RepositoryManager.get_directory_path();

        BranchManager.set_and_load_current_branch();
        textNameProject.setText("RECENT PROJECTS : current is " + directoryPath);
        recentProjects.push(directoryPath);
        update_recent_project_order();

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        show_buttons();

        show_recent_projects();
        update_state_of_project();
    }
    @FXML private void open_project_recent_project(String _directoryPath) {
        if (RepositoryManager.repository_doesnt_exist_at_this_path(_directoryPath)) {
            warning_project_not_found();
            return;
        }
        directoryPath = _directoryPath;
        textNameProject.setText("RECENT PROJECTS : current is " + directoryPath);

        RepositoryManager.set_directory_path(directoryPath);
        RepositoryManager.files_and_folders_exist();

        BranchManager.set_and_load_current_branch();

        recentProjects.push(directoryPath);
        update_recent_project_order();

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        show_buttons();

        show_recent_projects();
        update_state_of_project();
    }
    @FXML private void recheck_the_state() {
        if (RepositoryManager.repository_doesnt_exist_at_this_path(directoryPath)) {
            warning_project_not_found();
            return;
        }
        update_state_of_project();
    }

    private void warning_project_not_found(){
        Stage resultStage = new Stage();
        resultStage.setTitle("Project Not Found");
        Text text = new Text("Project is not found in this path");
        VBox vBox = new VBox(text);

        Scene resultScene = new Scene(vBox, 300, 200);
        resultStage.setScene(resultScene);
        resultStage.show();
    }
    private void update_state_of_project() {
        currentBranch.setText("Branch : " + BranchManager.getCurrentBranch().getName());
        FileManager.clear_current_state_of_project();
        FileManager.get_all_present_files_in_directory(directoryPath);
        show_the_state_of_the_project();
    }

    private int size_of_custom_check_boxes(Map<FileStateEnums, ArrayList<HashedFile>> map){
        int num = 0;
        if(map.containsKey(FileStateEnums.UNADDED )) num += map.get(FileStateEnums.UNADDED ).size();
        if(map.containsKey(FileStateEnums.ADDED   )) num += map.get(FileStateEnums.ADDED   ).size();
        if(map.containsKey(FileStateEnums.MODIFIED)) num += map.get(FileStateEnums.MODIFIED).size();
        if(map.containsKey(FileStateEnums.DELETED )) num += map.get(FileStateEnums.DELETED ).size();
        return num;
    }
    private void insert_state_title(FileStateEnums state){
        Text text = new Text();
        switch (state) {
            case ADDED -> text.setText("STAGED" + ": (select to unstage)\n");
            case DELETED -> text.setText("DELETED" + ": (select to restore)\n");
            case MODIFIED -> text.setText("MODIFIED" + ": (select to restore)\n");
            default -> text.setText("UNSTAGED" + ": (select to stage)\n");
        }
        currentFilesCheckBoxContainer.getChildren().add(text);
    }
    private void insert_staging_buttons(){
        stageUnstageButton = new Button("Stage/ Unstage");
        restoreButton= new Button("Restore");
        restageButton = new Button("Restage");

        stageUnstageButton.setOnAction(e -> stage_or_unstage_file());
        restoreButton.setOnAction(e -> restore_modified_or_deleted_file());
        restageButton.setOnAction(e -> restage_modified_file());

        executeButtonContainer.getChildren().clear();
        executeButtonContainer.getChildren().add(stageUnstageButton);
        executeButtonContainer.getChildren().add(restoreButton);
        executeButtonContainer.getChildren().add(restageButton);
    }
    private void show_the_state_of_the_project() {
        Map<FileStateEnums, ArrayList<HashedFile>> allChangesInThisDirectory;
        currentFilesCheckBoxContainer.getChildren().clear();

        allChangesInThisDirectory = StagingManager.detect_changes_in_staging_data();
        int numberOfCheckBoxes = size_of_custom_check_boxes(allChangesInThisDirectory);

        if(numberOfCheckBoxes == 0){
            Text text = new Text("Empty Folder add something!");
            currentFilesCheckBoxContainer.getChildren().add(text);
        }

        customHashedFilesCheckBoxes = new ArrayList<>(numberOfCheckBoxes);

        //goes into this order in the UI
        FileStateEnums[] orderStates = {FileStateEnums.ADDED, FileStateEnums.MODIFIED, FileStateEnums.DELETED, FileStateEnums.UNADDED};

        for (FileStateEnums state : orderStates) {
            if (!allChangesInThisDirectory.containsKey(state)) continue;

            ArrayList<HashedFile> arrayOfPaths = allChangesInThisDirectory.get(state);
            insert_state_title(state);

            String color = switch (state){
                case ADDED   -> "green";
                case UNADDED -> "red";
                default -> "black";
            };

            for (HashedFile hashedFile : arrayOfPaths) {
                CustomCheckBox<HashedFile> hashedFileToAdd = new CustomCheckBox<>(hashedFile, hashedFile.get_file_path(), color);
                hashedFileToAdd.setOnAction(event -> check_if_button_should_be_enabled_or_not(customHashedFilesCheckBoxes));
                customHashedFilesCheckBoxes.add(hashedFileToAdd);
                currentFilesCheckBoxContainer.getChildren().add(hashedFileToAdd);
            }
        }
        insert_staging_buttons();
        currentFilesCheckBoxContainer.getChildren().add(executeButtonContainer);
    }

    private void check_if_button_should_be_enabled_or_not(ArrayList<CustomCheckBox<HashedFile>> checkBoxes) {
        Set<FileStateEnums> checked = new HashSet<>();
        for (CustomCheckBox<HashedFile> checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                checked.add(checkBox.get_item().get_state());
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
    private void stage_or_unstage_file() {

        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_item();
                switch (hashedFile.get_state()) {
                    case UNADDED -> StagingManager.add_file_to_staging_data(hashedFile);
                    case ADDED -> StagingManager.delete_file_from_staging_data(hashedFile);
                }
            }
        }
        recheck_the_state();
    }
    private void restore_modified_or_deleted_file() {

        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_item();
                switch (hashedFile.get_state()) {
                    case DELETED, MODIFIED -> StagingManager.restore_file_from_staging_data(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheck_the_state();
    }
    private void restage_modified_file() {
        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.get_item();
                if (hashedFile.get_state() == FileStateEnums.MODIFIED) {
                    StagingManager.add_file_to_staging_data(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheck_the_state();
    }

    @SuppressWarnings("unchecked")
    private void load_recent_project_order(){
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
    private void update_recent_project_order(){

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

    @FXML private void open_folder() {
        System.out.println(directoryPath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe", directoryPath);
            processBuilder.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML private void commit_this_staging(){
        Stage commitStage = new Stage();
        commitStage.setTitle("COMMIT WINDOW");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("MESSAGE OF THIS REPO");
        TextArea textArea = new TextArea();

        Button button = new Button("Commit");
        button.setOnAction(e->
        {
            String userInput = textArea.getText();
            BranchManager.add_new_commit(userInput);

            recheck_the_state();
            commitStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(textArea);
        vbox.getChildren().add(button);

        Scene warningScene = new Scene(vbox,300,200);

        commitStage.setScene(warningScene);
        commitStage.show();
    }
    @FXML private void delete_this_repo(){
        Stage warningStage = new Stage();
        warningStage.setTitle("WARNING DELETE");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("Do you really want to delete this repo?");
        Button button = new Button("Confirm");
        button.setOnAction(e->
        {
            RepositoryManager.delete_this_repo();
            currentFilesCheckBoxContainer.getChildren().clear();
            textNameProject.setText("RECENT PROJECTS :");
            warningStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(button);

        Scene warningScene = new Scene(vbox,300,200);
        warningStage.setScene(warningScene);
        warningStage.show();
    }

    @FXML private void see_all_commits_in_order_button() {
        Stage warningStage = new Stage();
        warningStage.setTitle("COMMITS OF THIS BRANCH");

        VBox vbox = new VBox();

        ToggleGroup toggleGroup = new ToggleGroup();

        BranchManager.load_branch(BranchManager.load_which_branch_is_the_current());
        LinkedList<Commit> allCommits = BranchManager.getCurrentBranch().getAllCommits();

        Button submitButton = new Button("Checkout this commit");
        submitButton.setDisable(true);

        for(Commit commit : allCommits){
            CustomRadioButton<Commit> radioButton = new CustomRadioButton<>(commit, commit.getMessage(), "black");
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setOnAction(e->submitButton.setDisable(false));
            vbox.getChildren().add(radioButton);
        }

        submitButton.setOnAction(e -> {
            @SuppressWarnings("unchecked")
            CustomRadioButton<Commit> selectedRadioButton = (CustomRadioButton<Commit>) toggleGroup.getSelectedToggle();
            if (selectedRadioButton != null) {
                BranchManager.checkout_commit(selectedRadioButton.get_item());
            }
        });

        vbox.getChildren().add(submitButton);

        Scene warningScene = new Scene(vbox,300,200);
        warningStage.setScene(warningScene);
        warningStage.show();
    }
    @FXML private void branch_window_button() {
        Stage branchStage = new Stage();
        branchStage.setTitle("Branches of this project");

        VBox vbox = new VBox();

        ToggleGroup toggleGroup = new ToggleGroup();

        BranchManager.load_branch(BranchManager.load_which_branch_is_the_current());
        List<Branch> allBranches = BranchManager.get_all_available_branches();

        Button checkoutBranchButton = new Button("Checkout this branch");
        Button createBranchButton = new Button("New branch");
        Button deleteBranchButton = new Button("Delete branch");

        checkoutBranchButton.setDisable(true);

        for(Branch branch : allBranches){
            CustomRadioButton<Branch> radioButton = new CustomRadioButton<>(branch, branch.getName(), "black");
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setOnAction(e->checkoutBranchButton.setDisable(false));
            vbox.getChildren().add(radioButton);
        }

        checkoutBranchButton.setOnAction(e -> {
            @SuppressWarnings("unchecked")
            CustomRadioButton<Branch> selectedRadioButton = (CustomRadioButton<Branch>) toggleGroup.getSelectedToggle();
            if (selectedRadioButton != null) {
                Branch branch = selectedRadioButton.get_item();
                BranchManager.load_branch(branch.getName());
                currentBranch.setText("Branch : " + branch.getName());
                branchStage.close();
            }
        });

        createBranchButton.setOnAction(e ->
            handle_create_branch_window()
        );

        deleteBranchButton.setOnAction(e -> {
            @SuppressWarnings("unchecked")
            CustomRadioButton<Branch> selectedRadioButton = (CustomRadioButton<Branch>) toggleGroup.getSelectedToggle();
            String nameBranch = selectedRadioButton.get_item().getName();
            if(BranchManager.getCurrentBranch().getName().equals(nameBranch)){
                warning_window("Cant delete current branch");
                return;
            }
            BranchManager.delete_branch(nameBranch);
            branchStage.close();
            branch_window_button();
        });

        HBox hbox = new HBox();
        hbox.getChildren().add(checkoutBranchButton);
        hbox.getChildren().add(createBranchButton);
        hbox.getChildren().add(deleteBranchButton);

        vbox.getChildren().add(hbox);

        Scene warningScene = new Scene(vbox,300,200);
        branchStage.setScene(warningScene);
        branchStage.show();
    }
    private void handle_create_branch_window(){
        Stage addBranchStage = new Stage();
        addBranchStage.setTitle("ADD BRANCH WINDOW");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("NAME OF BRANCH");
        TextArea textArea = new TextArea();

        Button button = new Button("Add");
        button.setOnAction(e-> {
            String userInput = textArea.getText();
            if(BranchManager.branch_exist(userInput)){
                warning_window("Branch already exists");
                return;
            }
            BranchManager.add_or_save_branch(userInput);
            BranchManager.load_branch(userInput);

            recheck_the_state();
            addBranchStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(textArea);
        vbox.getChildren().add(button);

        Scene addBranchScene = new Scene(vbox,300,200);

        addBranchStage.setScene(addBranchScene);
        addBranchStage.show();
    }
    private void warning_window(String text){
        Stage w = new Stage();
        w.setTitle("WARNING WINDOW");

        VBox y = new VBox();
        y.setAlignment(Pos.CENTER);
        Scene x = new Scene(y,300,200);
        Text t = new Text(text);
        Button b = new Button("Ok");
        b.setOnAction(ev -> w.close());
        y.getChildren().add(t);
        y.getChildren().add(b);
        w.setScene(x);
        w.show();
    }

    private TextFlow changesLinesTextFlow;
    @FXML private void compare_two_files(){
        compare_files();
        show_line_changes();
        Stage resultStage = new Stage();
        resultStage.setTitle("Result Window");
        StackPane resultLayout = new StackPane();
        resultLayout.getChildren().add(changesLinesTextFlow); // Display the result (replace with appropriate UI)
        Scene resultScene = new Scene(resultLayout, 300, 200);
        resultStage.setScene(resultScene);
        resultStage.show();
    }
    private void compare_files() {
        File f = new File(directoryPath + "/720_576.ps1");
        File f2 = new File(directoryPath + "/720_576_1.ps1");

        ComparatorManager.set_old_file_path(f.getPath());
        ComparatorManager.set_new_file_path(f2.getPath());

        ComparatorManager.to_map_files();

        ComparatorManager.compare();
    }
    private void show_line_changes() {

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

}

