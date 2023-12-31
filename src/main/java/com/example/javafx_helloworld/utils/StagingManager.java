package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.enums.FileStateEnums;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.*;


public class StagingManager {

    public static boolean staging_doesnt_exist() {
        String filePath = RepositoryManager.PathOfStagingFile;

        File file = new File(filePath);

        return !file.exists();
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String,HashedFile> load_staging_data(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RepositoryManager.PathOfStagingFile))) {
            return (HashMap<String,HashedFile>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }
    private static void save_staging_data(HashMap<String, HashedFile> stagingData) {
        try (ObjectOutputStream outStagingFile = new ObjectOutputStream(new FileOutputStream(RepositoryManager.PathOfStagingFile))) {
            outStagingFile.writeObject(stagingData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add_file_to_staging_data(HashedFile addedFile){
        HashMap<String, HashedFile> stagingData = load_staging_data();

        String zipPath = addedFile.get_zip_path();
        String wholePathForFile = addedFile.get_file_path();
        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);

        addedFile.setState(FileStateEnums.ADDED);
        stagingData.put(wholePathForFile,addedFile);

        save_staging_data(stagingData);
    }
    public static void delete_file_from_staging_data(HashedFile file){
        HashMap<String, HashedFile> stagingData = load_staging_data();
        String filepath = file.get_file_path();
        stagingData.remove(filepath);

        save_staging_data(stagingData);
    }
    public static void restore_file_from_staging_data(HashedFile file) {
        String zipPath = file.get_zip_path();
        String wholePathForFile = file.get_file_path();

        CompressionManager.uncompress_file_content_into_its_place(zipPath, wholePathForFile);
    }

    private static boolean file_is_modified(HashedFile hashedFile, Map<String, HashedFile> allPresentFiles) {
        String filePath = hashedFile.get_file_path();
        return !hashedFile.get_hashed_name()
                .equals( allPresentFiles.get(filePath).get_hashed_name() );
    }
    public static HashMap<FileStateEnums, ArrayList<HashedFile>> detect_changes_in_staging_data(){
        Map<String, HashedFile> allPresentFiles = FileManager.get_all_present_files();
        Map<String, HashedFile> stagingData;

        if(StagingManager.staging_doesnt_exist()) {
            stagingData = new HashMap<>();
        }else {
            stagingData = load_staging_data();
        }

        HashMap<FileStateEnums, ArrayList<HashedFile>> changes = new HashMap<>();

        stagingData.forEach((filePath, hashedFile)->{
            boolean fileExist = allPresentFiles.containsKey(filePath);

            if(!fileExist){
                hashedFile.setState(FileStateEnums.DELETED);
                changes.computeIfAbsent(FileStateEnums.DELETED, k -> new ArrayList<>()).add(hashedFile);
                return;
            }

            FileStateEnums state ;

            if(file_is_modified(hashedFile, allPresentFiles))   state = FileStateEnums.MODIFIED;
            else if(!hashedFile.isCommited())                   state = FileStateEnums.ADDED;
            else                                                state = FileStateEnums.COMITED;


            hashedFile.setState(state);
            changes.computeIfAbsent(state, k -> new ArrayList<>()).add(hashedFile);

            allPresentFiles.remove(filePath);
        });

        allPresentFiles.forEach((filePath, hashedFile) -> {
            changes.computeIfAbsent(FileStateEnums.UNADDED, k -> new ArrayList<>()).add(hashedFile);
            hashedFile.setState(FileStateEnums.UNADDED);
        });

        return changes;
    }

    public static void update_staged_files_to_commited(){
        HashMap<String,HashedFile> stagingData = load_staging_data();

        stagingData.forEach((pathStagedFile, stagedFile)->{
            if(stagingData.get(pathStagedFile).get_state() == FileStateEnums.ADDED){
                stagedFile.setCommited(true);
            }
        });

        save_staging_data(stagingData);
    }
}

