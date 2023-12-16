package com.example.javafx_helloworld;

import java.io.*;
import java.util.*;

public class StateManager {


    static private String InitialDirectory;
    private static final String PathOfProjectState = "/.gitryad/stateFile";
    static String PathOfZippedFolders = "/.gitryad/ZippedFolders";

    private static final Map<String, Long> currentStateFiles = new HashMap<>();
    private static final Map<FileStateEnums, ArrayList<String>> currentChangesFiles = new HashMap<>();

    private static final Set<String> filesToIgnore = new HashSet<>();
    private static Map<String, String> stagedFiles = new HashMap<>();

    public static Map<String, Long> get_current_state_files() {
        return currentStateFiles;
    }

    public static Map<FileStateEnums, ArrayList<String>> get_current_changes_files() {
        return currentChangesFiles;
    }

    public static void set_initial_directory(String initialDirectory) {
        InitialDirectory = initialDirectory;
    }

    public static void empty_state_staging_maps() {
        currentStateFiles.clear();
        currentChangesFiles.clear();
    }

    public static void find_all_files(String current){

        //File current_directory = new File(InitialDirectory); not using that cuz of recursion
        retrieve_staging();
        File current_directory = new File(current);

        if(!current_directory.exists() || !current_directory.isDirectory()){
            return;
        }

        File[] files = current_directory.listFiles();
        if (files == null) return;

        for(File file : files){

            String filePath = file.getPath();
            if(check_if_file_is_ignored(file.getName()) || check_if_is_staged(file.getName())) {
                continue;
            }

            if(file.isDirectory()) {
                find_all_files(file.getPath());
            }else if (!stagedFiles.containsKey(filePath)) {
                currentChangesFiles.computeIfAbsent(FileStateEnums.UNADDED, k -> new ArrayList<>()).add(filePath);
            } else{
                currentStateFiles.put(filePath, file.lastModified());
            }
        }
    }


    public static boolean check_if_file_is_ignored(String filePathName) {
        return filesToIgnore.contains(filePathName);
    }

    public static boolean check_if_is_staged(String filePathName) {
        return stagedFiles.containsKey(filePathName);
    }




    public static void read_ignore_file(){
        String filePath = InitialDirectory + "/.gitignoreryad";
        File ignoredFoldersFile = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(ignoredFoldersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                filesToIgnore.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void retrieve_staging(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(InitialDirectory+PathOfZippedFolders + "/staging"))) {
            stagedFiles = (Map<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Return an empty map if the file doesn't exist (first run)
            stagedFiles = new HashMap<>();
        }
    }

//    public static void read_ignore_file(){
//        String filePath = InitialDirectory + "/.gitignoreryad"; // Correct the file name
//        read_file(filePath, filesToIgnore);
//    }
//    private static void read_file(String filePath, Set<String> set){
//        File stagingFileFolder = new File(filePath);
//        try (BufferedReader reader = new BufferedReader(new FileReader(stagingFileFolder))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                set.add(line.trim());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    public static void save_current_file_state(){
        System.out.println(InitialDirectory + PathOfProjectState);
        //create and save state file
        try (ObjectOutputStream outStateFile = new ObjectOutputStream(new FileOutputStream(InitialDirectory + PathOfProjectState))) {
            outStateFile.writeObject(currentStateFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
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

        currentStateFiles.forEach((filePath, currentModifiedTime) -> {

            Long previousModifiedTime = previousState.get(filePath);
            FileStateEnums fileStateEnums = determine_file_state(previousModifiedTime, currentModifiedTime);
            currentChangesFiles.computeIfAbsent(fileStateEnums, k -> new ArrayList<>()).add(filePath);
        });

        previousState.forEach((filePath, currentModifiedTime) -> {
            // this checks if its in the staged environement and deleted
            if (!currentStateFiles.containsKey(filePath)){
                currentChangesFiles.computeIfAbsent(FileStateEnums.DELETED, k -> new ArrayList<>()).add(filePath);
            }
        });
    }

    private static FileStateEnums determine_file_state(Long previousModifiedTime, Long currentModifiedTime) {
        if (previousModifiedTime != null && !previousModifiedTime.equals(currentModifiedTime)) {
            return FileStateEnums.MODIFIED;
        } else {
            // if its in the stagedFile and not modified then its added
            return FileStateEnums.ADDED;
        }
    }

    public static boolean saved_state_file_exist() {
        String filePath = InitialDirectory + PathOfProjectState;

        File file = new File(filePath);

        return file.exists();
    }
}
