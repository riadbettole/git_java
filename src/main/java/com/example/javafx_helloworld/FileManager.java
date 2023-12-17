package com.example.javafx_helloworld;

import java.io.*;
import java.util.*;

public class FileManager {

//    private static final Map<String, Long> currentStateFiles = new HashMap<>();
//    private static final Map<FileStateEnums, ArrayList<String>> currentChangesFiles = new HashMap<>();
//    private static Map<String, Long> previousState = new HashMap<>();


    private static final Map<String, HashedFile> allFilesPresent = new HashMap<>();


    private static final Set<String> filesToIgnore = new HashSet<>();
//    private static Map<String, String> stagedFiles = new HashMap<>();

//    public static Map<FileStateEnums, ArrayList<String>> get_current_changes_files() {
//        return currentChangesFiles;
//    }

//    public static Map<String, Long> get_current_state_files(){
//        return currentStateFiles;
//    }

    public static Map<String, HashedFile> get_all_files_present(){
        return allFilesPresent;
    }

//    public static void set_current_state_files(Map<String, Long> _currentStateFiles) {
//        currentStateFiles.clear();
//        currentStateFiles.putAll(_currentStateFiles);
//    }

//    public static void empty_state_staging_maps() {
//        currentStateFiles.clear();
//        currentChangesFiles.clear();
//    }

//    public static void find_all_files(String current){
//
//        //File current_directory = new File(InitialDirectory); not using that cuz of recursion
//        stagedFiles = StagingManager.retrieve_staging_data();
//        File current_directory = new File(current);
//
//        if(!current_directory.exists() || !current_directory.isDirectory()){
//            return;
//        }
//
//        File[] files = current_directory.listFiles();
//        if (files == null) return;
//
//        for(File file : files){
//
//            String filePath = file.getPath();
//            if(check_if_file_is_ignored(file.getName())) {
//                continue;
//            }
//
//            if(file.isDirectory()) {
//                find_all_files(file.getPath());
//            }else if (!stagedFiles.containsKey(filePath)) {
//                currentChangesFiles.computeIfAbsent(FileStateEnums.UNADDED, k -> new ArrayList<>()).add(filePath);
//            } else{
//                currentStateFiles.put(filePath, file.lastModified());
//            }
//        }
//    }

    public static void find_all_files(String current){

        //File current_directory = new File(InitialDirectory); not using that cuz of recursion
        File current_directory = new File(current);

        if(!current_directory.exists() || !current_directory.isDirectory()){
            return;
        }

        File[] files = current_directory.listFiles();
        if (files == null) return;

        read_ignore_file();

        for(File file : files){
            String filePath = file.getPath();
            if(check_if_file_is_ignored(file.getName())) {
                continue;
            }

            if(file.isDirectory()) {
                find_all_files(filePath);
            }else
                get_into_current_files(filePath);
        }

    }
    public static void get_into_current_files(String filePath){
        HashedFile sf = new HashedFile(filePath);
        sf.sha1_the_file();
        allFilesPresent.put(filePath,sf);
    }

    public static boolean check_if_file_is_ignored(String filePathName) {
        return filesToIgnore.contains(filePathName);
    }

    public static void read_ignore_file(){
        String filePath = RepositoryManager.PathOfIgnoreFile;
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

//    public static void save_current_file_state(){
//        System.out.println(InitialDirectory + PathOfProjectState);
//        //create and save state file
//        try (ObjectOutputStream outStateFile = new ObjectOutputStream(new FileOutputStream(InitialDirectory + PathOfProjectState))) {
//            outStateFile.writeObject(currentStateFiles);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public static void retrieve_previous_state(){
//        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(InitialDirectory + PathOfProjectState))) {
//            previousState = (Map<String, Long>) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            // Return an empty map if the file doesn't exist (first run)
//            previousState = new HashMap<>();
//        }
//    }

//    public static void detect_file_changes(){
//        retrieve_previous_state();
//        currentStateFiles.forEach((filePath, currentModifiedTime) -> {
//
//            Long previousModifiedTime = previousState.get(filePath);
//            FileStateEnums fileStateEnums = determine_file_state(previousModifiedTime, currentModifiedTime);
//            if(fileStateEnums == FileStateEnums.MODIFIED){
//                currentChangesFiles.computeIfAbsent(fileStateEnums, k -> new ArrayList<>()).add(filePath);
//                currentChangesFiles.computeIfAbsent(FileStateEnums.ADDED, k -> new ArrayList<>()).add(filePath);
//            }else {
//                currentChangesFiles.computeIfAbsent(fileStateEnums, k -> new ArrayList<>()).add(filePath);
//            }
//        });
//
//        previousState.forEach((filePath, currentModifiedTime) -> {
//            // this checks if its in the staged environement and deleted
//            if (stagedFiles.containsKey(filePath) && !currentStateFiles.containsKey(filePath)){
//                currentChangesFiles.computeIfAbsent(FileStateEnums.DELETED, k -> new ArrayList<>()).add(filePath);
//            }
//        });
//    }



//    public static boolean saved_state_file_exist() {
//        String filePath = InitialDirectory + PathOfProjectState;
//
//        File file = new File(filePath);
//
//        return file.exists();
//    }
}
