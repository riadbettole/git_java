package com.example.javafx_helloworld;

import java.io.*;
import java.util.*;

public class FileManager {


    static private String InitialDirectory;
    private static final String PathOfProjectState = "/.gitryad/meow";

    private static final Map<String, Long> currentStateFiles = new HashMap<>();
    private static final Map<FileState, ArrayList<String>> currentChangesFiles = new HashMap<>();

    private static final Set<String> foldersToIgnore = new HashSet<>();

    public static Map<String, Long> get_current_state_files() {
        return currentStateFiles;
    }

    public static Map<FileState, ArrayList<String>> get_current_changes_files() {
        return currentChangesFiles;
    }

    public static void set_initial_directory(String initialDirectory) {
        InitialDirectory = initialDirectory;
    }

    public static void find_all_files(String current){

        File current_directory = new File(current);

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

    public static void empty_current_file_state_array() {
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

    public static void save_current_file_state(){
        System.out.println(InitialDirectory + PathOfProjectState);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(InitialDirectory + PathOfProjectState))) {
            oos.writeObject(currentStateFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Long> retrieve_previous_state(String stateFilePath){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFilePath))) {
            return (Map<String, Long>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Return an empty map if the file doesn't exist (first run)
            return new HashMap<>();
        }
    }

    public static void detect_file_changes(){
        String prevFilePath = InitialDirectory + PathOfProjectState;
        Map<String, Long> previousState = retrieve_previous_state(prevFilePath);

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
        String filePath = InitialDirectory + PathOfProjectState;

        File file = new File(filePath);

        return file.exists();
    }
}
