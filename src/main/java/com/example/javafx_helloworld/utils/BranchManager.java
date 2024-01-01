package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Branch;
import com.example.javafx_helloworld.models.Commit;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class BranchManager {
    static HashMap<String, HashedFile> stagingData;
    static public Branch currentBranch;

    static public void save_which_branch_is_the_current(String Name){
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/currentBranch", Name);
    }
    static public String load_which_branch_is_the_current(){
        return LoadingSavingManager.load_item(RepositoryManager.PathOfBranchFolder + "/currentBranch");
    }
    public static boolean current_branch_doesnt_exist() {
        String filePath = RepositoryManager.PathOfBranchFolder + "/currentBranch";
        File file = new File(filePath);
        return !file.exists();
    }

    static public void load_branch(String name){
        save_which_branch_is_the_current(name);
        if(branch_exist(name)){
            currentBranch = LoadingSavingManager.load_item(RepositoryManager.PathOfBranchFolder + "/" + name);
        }
//        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
//        Commit lastCommit = allCommits.getLast();
//        checkout_commit(lastCommit.getMessage());
    }

    static public Commit find_this_commit(String commitName){
        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
        for(Commit commit: allCommits){
            if(commit.getMessage().equals(commitName)){
                return commit;
            }
        }
        throw new RuntimeException("commit not found");
    }

    static public void checkout_commit(String commitName){
        Commit commitToCheckout = find_this_commit(commitName);
        load_files_of_commit_X_to_place(commitToCheckout);
    }

    public static void load_files_of_commit_X_to_place(Commit commitToPlace){
        String pathOfZippedStaging = commitToPlace.get_zipped_commited_stagingPath();
        stagingData = LoadingSavingManager.load_item(RepositoryManager.PathOfStagingFile);

        File f = new File(RepositoryManager.PathOfStagingFile);
        if(!f.delete()) return;

        File currentDirectory = new File(RepositoryManager.PathOfRepository);
        delete_all_files(currentDirectory.getParentFile());

        CompressionManager.uncompress_file_content_into_its_place(pathOfZippedStaging, RepositoryManager.PathOfStagingFile);
        stagingData = LoadingSavingManager.load_item(RepositoryManager.PathOfStagingFile);
        stagingData.forEach((path,hashedFile)-> CompressionManager.uncompress_file_content_into_its_place(hashedFile.get_zip_path(), hashedFile.get_file_path()) );
    }
    private static void delete_all_files(File current_directory) {
        File [] files = current_directory.listFiles();
        assert files != null;
        Set<String> filesToIgnore = FileManager.load_ignore_file();
        for (File subfile : files) {
            if(FileManager.check_if_file_is_ignored(filesToIgnore, subfile.getName()))
                continue;

            if (subfile.isDirectory()) {
                delete_all_files(subfile);
            }

            subfile.delete();
        }
    }

    public static void add_new_commit(String message) {
        if(current_branch_doesnt_exist()){
            currentBranch = new Branch("Master");
        }else {
            String nameOfCurrentBranch = load_which_branch_is_the_current();
            load_branch(nameOfCurrentBranch);
        }

        HashedFile hashedStagingFile = new HashedFile(RepositoryManager.PathOfStagingFile);

        String zipPath = hashedStagingFile.get_zip_path();
        String wholePathForFile = hashedStagingFile.get_file_path();
        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);

        Commit newCommit = new Commit(message, "ryad", hashedStagingFile.get_zip_path());
        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
        allCommits.add(newCommit);

        StagingManager.update_staged_files_to_commited();

        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/"+ currentBranch.getName(), currentBranch);
        save_which_branch_is_the_current(currentBranch.getName());
    }

    static boolean branch_exist(String name){
        File file = new File(RepositoryManager.PathOfBranchFolder + "/" + name);
        return file.exists();
    }

    public static Branch getCurrentBranch() {
        return currentBranch;
    }
}

