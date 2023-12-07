package com.example.javafx_helloworld;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

enum FileState {
    ADDED, MODIFIED, PREVIOUS, DELETED;
}

public class HelloApplication extends Application {

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("File Manager!");
        stage.setScene(scene);

        stage.show();



    }

    public static void main(String[] args) {
        launch();
    }
}


class File_manager {
    static String InitialDirectory;
    static File current_directory;
    static Map<String, Long> currentStateFiles = new HashMap<>();
    static Map<FileState, ArrayList<String>> currentChangesFiles = new HashMap<>();
    private static final Set<String> foldersToIgnore = new HashSet<>();
    public static void find_all_files(String current){

        current_directory = new File(current);

        if(!current_directory.exists() || !current_directory.isDirectory()){
            return;
        }

        File[] files = current_directory.listFiles();
        if (files == null) return;

        for(File file : files){

            if(check_if_file_is_ignored(file.getName())) {
                continue;
            }

            if(file.isDirectory()) {
                find_all_files(file.getPath());
            }else {
                currentStateFiles.put(file.getPath(), file.lastModified());
            }
        }
    }


    public static void empty() {
        currentStateFiles.clear();
    }

    public static boolean check_if_file_is_ignored(String folderName) {
        return foldersToIgnore.contains(folderName);
    }

    public static void read_ignore_file(){
        String filePath = InitialDirectory + "/.gitignoreryad";
        File ignoredFoldersFile = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(ignoredFoldersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                foldersToIgnore.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save_state(){
        System.out.println(InitialDirectory+"/.gitryad/meow");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(InitialDirectory+"/.gitryad/meow"))) {
            oos.writeObject(currentStateFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Long> retrievePreviousState(String stateFilePath){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFilePath))) {
            return (Map<String, Long>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Return an empty map if the file doesn't exist (first run)
            return new HashMap<>();
        }
    }

    public static void detectChanges(){
        String prevFilePath = InitialDirectory + "/.gitryad/meow";
        Map<String, Long> previousState = retrievePreviousState(prevFilePath);

        currentChangesFiles.clear();

        currentStateFiles.forEach((filePath, currentModifiedTime) -> {

            Long previousModifiedTime = previousState.get(filePath);

            if (previousModifiedTime == null) {
                currentChangesFiles.computeIfAbsent(FileState.ADDED, k -> new ArrayList<>()).add(filePath);
            } else if (!previousModifiedTime.equals(currentModifiedTime)) {
                currentChangesFiles.computeIfAbsent(FileState.MODIFIED, k -> new ArrayList<>()).add(filePath);
            } else {
                currentChangesFiles.computeIfAbsent(FileState.PREVIOUS, k -> new ArrayList<>()).add(filePath);
            }
        });

        previousState.forEach((filePath, currentModifiedTime) -> {
            if (!currentStateFiles.containsKey(filePath)){
                currentChangesFiles.computeIfAbsent(FileState.DELETED, k -> new ArrayList<>()).add(filePath);
//                currentChangesFiles.get(FileState.DELETED).add(filePath);
            }
        });
    }

    public static boolean saved_state_exist() {
        String filePath = InitialDirectory + "/.gitryad/meow";

        File file = new File(filePath);

        return file.exists();
    }
}