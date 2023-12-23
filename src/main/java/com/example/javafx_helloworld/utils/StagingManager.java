package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.enums.FileStateEnums;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class StagingManager {

    public static boolean saved_state_file_doesnt_exist() {
        File file = new File(RepositoryManager.PathOfStagingFile);

        return !file.exists();
    }

    public static void create_state_file() {
        File file = new File(RepositoryManager.PathOfStagingFile);
        try{
            Files.createFile(file.toPath());
            HashMap<String,HashedFile> x = new HashMap<>();
            HashedFile y = new HashedFile("x");
            x.put("x", y);
            save_staging_data(x);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    private static void create_zipped_file_folder(String parentPath){
        Path path = Paths.get(parentPath);
        try{
            Files.createDirectories(path);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private static boolean zipped_file_folder_doesnt_exist(String filePath) {
        File file = new File(filePath);
        return !file.exists();
    }

    private static void update_staging(HashedFile file){
        // get and update old staging to make a new one
        String wholePathForFile = file.get_file_path();
        HashMap<String,HashedFile> stagingData = load_staging_data();
        stagingData.put(wholePathForFile,file);

        save_staging_data(stagingData);
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

    public static void add_file_to_staging(HashedFile file){
        file.sha1_the_file();
        file.setup_file_parent_and_zip_path();

        if(zipped_file_folder_doesnt_exist(file.get_file_path())) {
            create_zipped_file_folder(file.get_parent_path());
        }

        String zipPath = file.get_zip_path();
        String wholePathForFile = file.get_file_path();
        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);
        file.setState(FileStateEnums.ADDED);
        update_staging(file);
            
    }
    
    public static void delete_file_from_staging(HashedFile file){
        // get and update old staging to make a new one
        HashMap<String, HashedFile> stagingData = load_staging_data();
        String filepath = file.get_file_path();
        stagingData.remove(filepath);

        save_staging_data(stagingData);
    }

    public static void restore_file_from_staging(HashedFile file) {
        String zipPath = file.get_zip_path();
        String wholePathForFile = file.get_file_path();

        CompressionManager.uncompress_file_content_into_its_place(zipPath, wholePathForFile);
    }

    public static HashMap<FileStateEnums, ArrayList<HashedFile>> detect_staged_file_changes(){
        Map<String, HashedFile> allPresentFiles = FileManager.get_all_files_present();
        Map<String,HashedFile> stagingData = load_staging_data();

        HashMap<FileStateEnums, ArrayList<HashedFile>> changes = new HashMap<>();

        stagingData.forEach((filePath, hashedFile)->{
            boolean fileExist = allPresentFiles.containsKey(filePath);

            if(!fileExist){
                hashedFile.setState(FileStateEnums.DELETED);
                changes.computeIfAbsent(FileStateEnums.DELETED, k -> new ArrayList<>()).add(hashedFile);
                return;
            }

            FileStateEnums state ;

            if(file_is_not_modified(hashedFile, allPresentFiles) && !hashedFile.isCommited()){
                state = FileStateEnums.ADDED;
            }else{
                state = FileStateEnums.MODIFIED;
            }

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

    private static boolean file_is_not_modified(HashedFile hashedFile, Map<String, HashedFile> allPresentFiles) {
        String filePath = hashedFile.get_file_path();
        return hashedFile.get_hashed_name()
                .equals( allPresentFiles.get(filePath).get_hashed_name() );
    }

    private  static void clear_staging(){
        HashMap<String,HashedFile> stagingData = load_staging_data();
        stagingData.forEach((k,v)->{
            if(stagingData.get(k).get_state() == FileStateEnums.ADDED){
                v.setCommited(true);
            }
        });
        save_staging_data(stagingData);
    }
}

