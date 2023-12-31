package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Commit;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.LinkedList;

public class CommitManager {
    @SuppressWarnings("unchecked")
    static public LinkedList<Commit> load_commit_file(){
        LinkedList<Commit> allPrecedentCommits;
        try(ObjectInputStream n = new ObjectInputStream(new FileInputStream(RepositoryManager.PathOfCommitFile))){
            allPrecedentCommits = (LinkedList<Commit>) n.readObject();
        }catch(ClassNotFoundException | IOException e){
            allPrecedentCommits = new LinkedList<>();
        }
        return allPrecedentCommits;
    }
    static public void add_new_commit(String message){
        //we hash the staging file and zip it and put it in commit
        HashedFile hashedStagingFile = new HashedFile(RepositoryManager.PathOfStagingFile);

        String zipPath = hashedStagingFile.get_zip_path();
        String wholePathForFile = hashedStagingFile.get_file_path();
        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);

        Commit newCommit = new Commit(message, "ryad", hashedStagingFile.get_zip_path());
        save_new_commit(newCommit);
        StagingManager.update_staged_files_to_commited();
    }
    static private void save_new_commit(Commit newCommit) {
        LinkedList<Commit> updatedCommits;

        if(commit_file_doesnt_exist()) {
            updatedCommits = new LinkedList<>();
        } else {
            updatedCommits = load_commit_file();
        }

        updatedCommits.add(newCommit);

        try(ObjectOutputStream n = new ObjectOutputStream(new FileOutputStream(RepositoryManager.PathOfCommitFile))){
            n.writeObject(updatedCommits);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    public static boolean commit_file_doesnt_exist() {
        String filePath = RepositoryManager.PathOfCommitFile;
        File file = new File(filePath);
        return !file.exists();
    }
}
