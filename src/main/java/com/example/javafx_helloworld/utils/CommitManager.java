package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class CommitManager {
    @SuppressWarnings("unchecked")
    LinkedList<Commit> load_commit_file(String path){
        LinkedList<Commit> x;
        try(ObjectInputStream n = new ObjectInputStream(new FileInputStream(path))){
            x = (LinkedList<Commit>) n.readObject();
        }catch(ClassNotFoundException | IOException e){
            x = new LinkedList<>();
        }
        return x;
    }
    void save_current_staging(HashedFile file){
        file.sha1_the_file();
        file.setup_file_parent_and_zip_path();

        String zipPath = file.get_zip_path();
        String wholePathForFile = file.get_file_path();

        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);
    }
    void new_commit(String message){
        HashedFile file = new HashedFile(RepositoryManager.PathOfStagingFile);
        save_current_staging(file);
        Commit c = new Commit(message, "ryad", file.get_zip_path());
        save_commit_info(c);
    }

//    public static void create_commit_file() {
//        Path path = Paths.get(RepositoryManager.PathOfCommitFile);
//        try {
//            Files.createFile(path);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static boolean ignored_files_doesnt_exist() {
//        String filePath = RepositoryManager.PathOfCommitFile;
//
//        File file = new File(filePath);
//
//        return !file.exists();
//    }

    private void save_commit_info(Commit c) {
        LinkedList<Commit> x = load_commit_file(RepositoryManager.PathOfCommitFile);
        x.add(c);
        try(ObjectOutputStream n = new ObjectOutputStream(new FileOutputStream(c.hashedCommitedStagingPath))){
            n.writeObject(x);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}

class Commit {
    String message;
    String commiter;
    String hashedCommitedStagingPath;
    LocalDateTime currentTime;

    public Commit(String message, String commiter, String hashedCommitedStagingPath) {
        this.message = message;
        this.commiter = commiter;
        this.hashedCommitedStagingPath = hashedCommitedStagingPath;
        currentTime = LocalDateTime.now();
    }
}
