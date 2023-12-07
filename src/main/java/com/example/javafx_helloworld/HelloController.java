package com.example.javafx_helloworld;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HelloController {

    @FXML
    private TextArea filesTextArea;
    @FXML
    private TextFlow changesTextFlow;

    private String directoryPath;

    @FXML
    protected void handleShowFiles() {
        handle_folder_selection();

        if(directoryPath == null || directoryPath.isEmpty()) return;

        File_manager.InitialDirectory = directoryPath;
        File_manager.read_ignore_file();

        update_everything();
    }
    protected void handle_folder_selection(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        directoryChooser.setInitialDirectory(new File("."));
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null)
            return;

        directoryPath = selectedDirectory.getAbsolutePath();

        if(repository_dont_exist())
            createRepositoryFolder();

        if(ignored_files_dont_exist())
            createIgnoreFile();

        File_manager.empty();
    }

    public void update_everything(){
        filesTextArea.clear();

        File_manager.find_all_files(directoryPath);

        File_manager.currentStateFiles
                .forEach((filePath, v) -> filesTextArea.appendText(filePath + "\n"));

        if(File_manager.saved_state_exist()) {
            File_manager.detectChanges();
            changesTextFlow.getChildren().clear();
            show_changes();
        }
        File_manager.save_state();
    }

    public void recheckFiles(){
        File_manager.empty();
        update_everything();
    }

    public void show_changes(){

        File_manager.currentChangesFiles
                .forEach((state, arrayOfPaths) -> {
                    Color color ;
                    switch (state) {
                        case ADDED -> color = Color.GREEN;
                        case MODIFIED -> color = Color.YELLOW;
                        case DELETED -> color = Color.RED;
                        default -> color = Color.BLACK;
                    };
                    arrayOfPaths.forEach( file -> {
                        Text text = new Text(file + "\n");
                        text.setFill(color);
                        changesTextFlow.getChildren().add(text);
                    });
                });
    }
    private void createRepositoryFolder() {
        Path path = Paths.get(directoryPath+"/.gitryad");
        try {
            Files.createDirectories(path);
            Files.setAttribute(path, "dos:hidden", true);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private void createIgnoreFile() {
        Path path = Paths.get(directoryPath+"/.gitignoreryad");
        try{
            Files.createFile(path);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(".gitryad\n");
            }
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public boolean ignored_files_dont_exist() {
        String filePath = directoryPath + "/.gitignoreryad";

        File file = new File(filePath);

        return !file.exists();
    }

    public boolean repository_dont_exist() {
        String filePath = directoryPath + "/.gitryad";

        File file = new File(filePath);

        return !file.exists();
    }

}