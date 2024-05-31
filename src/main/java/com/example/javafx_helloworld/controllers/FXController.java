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

    @FXML private Button pushButton;
    @FXML private Button pullButton;
    @FXML private Button connectButton;

    CustomStack<String> recentProjects;

    ArrayList<CustomCheckBox<HashedFile>> customHashedFilesCheckBoxes;

    Button stageUnstageButton;
    Button restoreButton;
    Button restageButton;

    private void showButtons() {
        compareFilesButton.setVisible(true);
        commitButton      .setVisible(true);
        deleteRepoButton  .setVisible(true);
        recheckFilesButton.setVisible(true);
        openFolderButton  .setVisible(true);
        checkoutButton    .setVisible(true);
        branchWindowButton.setVisible(true);
        connectButton     .setVisible(true);
    }
    @FXML private void openProject() {
        RepositoryManager.selectFolderOfRepository();
        directoryPath = RepositoryManager.getDirectoryPath();

        pushButton.setVisible(false);
        pullButton.setVisible(false);
        RepositoryManager.RemoteUrlRepository = "";
        RepositoryManager.KeyRepository = "";

        BranchManager.setAndLoadCurrentBranch();
        textNameProject.setText("RECENT PROJECTS : current is " + directoryPath);
        recentProjects.push(directoryPath);
        updateRecentProjectOrder();

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        showButtons();

        showRecentProjects();
        updateStateOfProject();
    }
    @FXML private void openProjectRecentProject(String _directoryPath) {
        if (RepositoryManager.repositoryDoesntExistAtThisPath(_directoryPath)) {
            warningProjectNotFound();
            return;
        }
        directoryPath = _directoryPath;
        textNameProject.setText("RECENT PROJECTS : current is " + directoryPath);

        pushButton.setVisible(false);
        pullButton.setVisible(false);
        RepositoryManager.RemoteUrlRepository = "";
        RepositoryManager.KeyRepository = "";

        RepositoryManager.setDirectoryPath(directoryPath);
        RepositoryManager.initializeRepositoryFoldersAndFiles();

        BranchManager.setAndLoadCurrentBranch();

        recentProjects.push(directoryPath);
        updateRecentProjectOrder();

        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }

        showButtons();

        showRecentProjects();
        updateStateOfProject();
    }
    @FXML private void recheckTheState() {
        if (RepositoryManager.repositoryDoesntExistAtThisPath(directoryPath)) {
            warningProjectNotFound();
            return;
        }
        updateStateOfProject();
    }

    private void warningProjectNotFound(){
        Stage resultStage = new Stage();
        resultStage.setTitle("Project Not Found");
        Text text = new Text("Project is not found in this path");
        VBox vBox = new VBox(text);

        Scene resultScene = new Scene(vBox, 300, 200);
        resultStage.setScene(resultScene);
        resultStage.show();
    }
    private void updateStateOfProject() {
        currentBranch.setText("Branch : " + BranchManager.getCurrentBranch().getName());
        FileManager.clearCurrentStateOfProject();
        FileManager.detectAllPresentFilesInThisDirectory(directoryPath);
        showTheStateOfTheProject();
    }

    private int sizeOfCustomCheckBoxes(Map<FileStateEnums, ArrayList<HashedFile>> map){
        int num = 0;
        if(map.containsKey(FileStateEnums.UNADDED )) num += map.get(FileStateEnums.UNADDED ).size();
        if(map.containsKey(FileStateEnums.ADDED   )) num += map.get(FileStateEnums.ADDED   ).size();
        if(map.containsKey(FileStateEnums.MODIFIED)) num += map.get(FileStateEnums.MODIFIED).size();
        if(map.containsKey(FileStateEnums.DELETED )) num += map.get(FileStateEnums.DELETED ).size();
        return num;
    }
    private void insertStateTitle(FileStateEnums state){
        Text text = new Text();
        switch (state) {
            case ADDED -> text.setText("STAGED" + ": (select to unstage)\n");
            case DELETED -> text.setText("DELETED" + ": (select to restore)\n");
            case MODIFIED -> text.setText("MODIFIED" + ": (select to restore)\n");
            default -> text.setText("UNSTAGED" + ": (select to stage)\n");
        }
        currentFilesCheckBoxContainer.getChildren().add(text);
    }
    private void insertStagingButtons(){
        stageUnstageButton = new Button("Stage/ Unstage");
        restoreButton= new Button("Restore");
        restageButton = new Button("Restage");

        stageUnstageButton.setOnAction(e -> stageOrUnstageFile());
        restoreButton.setOnAction(e -> restoreModifiedOrDeletedFile());
        restageButton.setOnAction(e -> restageModifiedFile());

        executeButtonContainer.getChildren().clear();
        executeButtonContainer.getChildren().add(stageUnstageButton);
        executeButtonContainer.getChildren().add(restoreButton);
        executeButtonContainer.getChildren().add(restageButton);
    }
    private void showTheStateOfTheProject() {
        Map<FileStateEnums, ArrayList<HashedFile>> allChangesInThisDirectory;
        currentFilesCheckBoxContainer.getChildren().clear();

        allChangesInThisDirectory = StagingManager.detectChangesInStagingData();
        int numberOfCheckBoxes = sizeOfCustomCheckBoxes(allChangesInThisDirectory);

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
            insertStateTitle(state);

            String color = switch (state){
                case ADDED   -> "green";
                case UNADDED -> "red";
                default -> "black";
            };

            for (HashedFile hashedFile : arrayOfPaths) {
                CustomCheckBox<HashedFile> hashedFileToAdd = new CustomCheckBox<>(hashedFile, hashedFile.get_file_path(), color);
                hashedFileToAdd.setOnAction(event -> checkIfButtonShouldBeEnabledOrNot(customHashedFilesCheckBoxes));
                customHashedFilesCheckBoxes.add(hashedFileToAdd);
                currentFilesCheckBoxContainer.getChildren().add(hashedFileToAdd);
            }
        }
        insertStagingButtons();
        currentFilesCheckBoxContainer.getChildren().add(executeButtonContainer);
    }

    private void checkIfButtonShouldBeEnabledOrNot(ArrayList<CustomCheckBox<HashedFile>> checkBoxes) {
        Set<FileStateEnums> checked = new HashSet<>();
        for (CustomCheckBox<HashedFile> checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                checked.add(checkBox.getItem().get_state());
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
    private void stageOrUnstageFile() {

        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.getItem();
                switch (hashedFile.get_state()) {
                    case UNADDED -> StagingManager.addFileToStagingArea(hashedFile);
                    case ADDED -> StagingManager.removeFromStagingArea(hashedFile);
                }
            }
        }
        recheckTheState();
    }
    private void restoreModifiedOrDeletedFile() {

        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.getItem();
                switch (hashedFile.get_state()) {
                    case DELETED, MODIFIED -> StagingManager.restoreFileFromStagingData(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheckTheState();
    }
    private void restageModifiedFile() {
        for (CustomCheckBox<HashedFile> customCheckBox : customHashedFilesCheckBoxes) {
            if (customCheckBox.isSelected()) {
                HashedFile hashedFile = customCheckBox.getItem();
                if (hashedFile.get_state() == FileStateEnums.MODIFIED) {
                    StagingManager.addFileToStagingArea(hashedFile);
                }

                System.out.println(hashedFile.get_file_path());
            }
        }
        restoreButton.setDisable(false);
        restageButton.setDisable(false);
        recheckTheState();
    }

    @SuppressWarnings("unchecked")
    private void loadRecentProjectOrder(){
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
    private void updateRecentProjectOrder(){

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
    public void showRecentProjects(){
        loadRecentProjectOrder();
        recentProjectsContainer.getChildren().clear();

        CustomStack<String> reverseString = new CustomStack<>(recentProjects);

        Collections.reverse(reverseString);

        for(String path: reverseString){
            Hyperlink hyperlink = new Hyperlink(path);
            hyperlink.setOnAction(event -> {
                String hyperlinkText = hyperlink.getText();
                openProjectRecentProject(hyperlinkText);
            });
            recentProjectsContainer.getChildren().add(hyperlink);
        }
    }

    @FXML private void openFolder() {
        System.out.println(directoryPath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe", directoryPath);
            processBuilder.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML private void commitThisStaging(){
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
            BranchManager.addNewCommit(userInput);

            recheckTheState();
            commitStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(textArea);
        vbox.getChildren().add(button);

        Scene warningScene = new Scene(vbox,300,200);

        commitStage.setScene(warningScene);
        commitStage.show();
    }
    @FXML private void deleteThisRepo(){
        Stage warningStage = new Stage();
        warningStage.setTitle("WARNING DELETE");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("Do you really want to delete this repo?");
        Button button = new Button("Confirm");
        button.setOnAction(e->
        {
            RepositoryManager.deleteThisRepository();
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

    @FXML private void seeAllCommitsInOrderButton() {
        Stage warningStage = new Stage();
        warningStage.setTitle("COMMITS OF THIS BRANCH");

        VBox vbox = new VBox();

        ToggleGroup toggleGroup = new ToggleGroup();

        BranchManager.loadBranch(BranchManager.loadWhichBranchIsTheCurrent());
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
                BranchManager.checkoutCommit(selectedRadioButton.getItem());
            }
        });

        vbox.getChildren().add(submitButton);

        Scene warningScene = new Scene(vbox,300,200);
        warningStage.setScene(warningScene);
        warningStage.show();
    }
    @FXML private void branchWindowButton() {
        Stage branchStage = new Stage();
        branchStage.setTitle("Branches of this project");

        VBox vbox = new VBox();

        ToggleGroup toggleGroup = new ToggleGroup();

        BranchManager.loadBranch(BranchManager.loadWhichBranchIsTheCurrent());
        List<Branch> allBranches = BranchManager.getAllAvailableBranches();

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
                Branch branch = selectedRadioButton.getItem();
                BranchManager.loadBranch(branch.getName());
                currentBranch.setText("Branch : " + branch.getName());
                branchStage.close();
            }
        });

        createBranchButton.setOnAction(e ->
            handleCreateBranchWindow()
        );

        deleteBranchButton.setOnAction(e -> {
            @SuppressWarnings("unchecked")
            CustomRadioButton<Branch> selectedRadioButton = (CustomRadioButton<Branch>) toggleGroup.getSelectedToggle();
            String nameBranch = selectedRadioButton.getItem().getName();
            if(BranchManager.getCurrentBranch().getName().equals(nameBranch)){
                warningWindow("Cant delete current branch");
                return;
            }
            BranchManager.delete_branch(nameBranch);
            branchStage.close();
            branchWindowButton();
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
    private void handleCreateBranchWindow(){
        Stage addBranchStage = new Stage();
        addBranchStage.setTitle("ADD BRANCH WINDOW");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("NAME OF BRANCH");
        TextArea textArea = new TextArea();

        Button button = new Button("Add");
        button.setOnAction(e-> {
            String userInput = textArea.getText();
            if(BranchManager.branchExist(userInput)){
                warningWindow("Branch already exists");
                return;
            }
            BranchManager.addOrSaveBranch(userInput);
            BranchManager.loadBranch(userInput);

            recheckTheState();
            addBranchStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(textArea);
        vbox.getChildren().add(button);

        Scene addBranchScene = new Scene(vbox,300,200);

        addBranchStage.setScene(addBranchScene);
        addBranchStage.show();
    }
    private void warningWindow(String text){
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
    @FXML private void compareTwoFiles(){
        compareFiles();
        showLineChanges();
        Stage resultStage = new Stage();
        resultStage.setTitle("Result Window");
        StackPane resultLayout = new StackPane();
        resultLayout.getChildren().add(changesLinesTextFlow); // Display the result (replace with appropriate UI)
        Scene resultScene = new Scene(resultLayout, 300, 200);
        resultStage.setScene(resultScene);
        resultStage.show();
    }
    private void compareFiles() {
        File f = new File(directoryPath + "/720_576.ps1");
        File f2 = new File(directoryPath + "/720_576_1.ps1");

        ComparatorManager.setOldFilePath(f.getPath());
        ComparatorManager.setNewFilePath(f2.getPath());

        ComparatorManager.toMapFiles();

        ComparatorManager.compare();
    }
    private void showLineChanges() {

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

    @FXML private void pushThisBranch(){
        BranchManager.pushBranch();
    }
    @FXML private void pullAllbranches(){
//        BranchManager.pull();
    }
    @FXML private void connectToRepo(){
        Stage connectStage = new Stage();
        connectStage.setTitle("CONNECT WINDOW");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        Text text = new Text("REMOTE URL OF REPOSITORY");
        TextField textField = new TextField();

        Text text2 = new Text("KEY OF REPOSITORY");
        TextField textField2 = new TextField();

        Button button = new Button("Connect");
        button.setOnAction(e->
        {
            String userInput = textField.getText();
            String userInput2 = textField2.getText();

            RepositoryManager.setKeyAndUrl(userInput, userInput2);

            pushButton.setVisible(true);
            pullButton.setVisible(true);

            connectStage.close();
        });

        vbox.getChildren().add(text);
        vbox.getChildren().add(textField);
        vbox.getChildren().add(text2);
        vbox.getChildren().add(textField2);
        vbox.getChildren().add(button);

        Scene warningScene = new Scene(vbox,300,200);

        connectStage.setScene(warningScene);
        connectStage.show();
    }
}

