package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Branch;
import com.example.javafx_helloworld.models.Commit;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class BranchManager {
    static HashMap<String, HashedFile> stagingData;
    static private Branch currentBranch;
    static final private String FirstBranch = "Master";
    static final private String NameOfCurrentBranchFile = "currentBranch";

    static public ArrayList<Branch> get_all_available_branches(){
        ArrayList<Branch> allBranches = new ArrayList<>();
        File f = new File(RepositoryManager.PathOfBranchFolder);
        File[] files = f.listFiles();
        if (files == null) {
            throw new RuntimeException("No files");
        }

        for(File subfile : files){
            String name = subfile.getName();
            if(!name.equals(NameOfCurrentBranchFile))
                allBranches.add(LoadingSavingManager.load_item(RepositoryManager.PathOfBranchFolder + "/" + name));
        }

        return allBranches;
    }

    static public void save_which_branch_is_the_current(String Name){
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/" + NameOfCurrentBranchFile, Name);
    }
    static public String load_which_branch_is_the_current(){
        return LoadingSavingManager.load_item(RepositoryManager.PathOfBranchFolder + "/" + NameOfCurrentBranchFile);
    }

    public static void set_and_load_current_branch(){
        if(!branch_exist(FirstBranch))
            save_which_branch_is_the_current(FirstBranch);
        load_branch(load_which_branch_is_the_current());
    }

    static public void load_branch(String name){
        save_which_branch_is_the_current(name);
        if(branch_exist(name)){
            currentBranch = LoadingSavingManager.load_item(RepositoryManager.PathOfBranchFolder + "/" + name);
        }else{
            currentBranch = new Branch(FirstBranch);
            add_or_save_branch(FirstBranch);
        }
    }

    static public void checkout_commit(Commit commit){
        load_files_of_commit_X_to_place(commit);
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
            if(!stagingData.containsKey(subfile.getPath()) | FileManager.check_if_file_is_ignored(filesToIgnore, subfile.getName()))
                continue;

            if (subfile.isDirectory()) {
                delete_all_files(subfile);
            }

            if(!subfile.delete())
                throw new RuntimeException("error in delete");
        }
    }

    public static void add_new_commit(String message) {
        HashedFile hashedStagingFile = new HashedFile(RepositoryManager.PathOfStagingFile);

        String zipPath = hashedStagingFile.get_zip_path();
        String wholePathForFile = hashedStagingFile.get_file_path();
        CompressionManager.compress_file_content_into_zipped_folder(wholePathForFile, zipPath);

        Commit newCommit = new Commit(message, "ryad", hashedStagingFile.get_zip_path());
        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
        allCommits.add(newCommit);

        StagingManager.update_staged_files_to_commited();

        add_or_save_branch(currentBranch.getName());
        save_which_branch_is_the_current(currentBranch.getName());
    }

    static public boolean branch_exist(String name){
        File file = new File(RepositoryManager.PathOfBranchFolder + "/" + name);
        return file.exists();
    }

    static public Branch getCurrentBranch() {
        return currentBranch;
    }

    static public void add_or_save_branch(String nameOfBranch){
        Branch branch = currentBranch;
        branch.setName(nameOfBranch);
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/"+ nameOfBranch , branch);
    }

    static public void delete_branch(String nameOfBranch){
        if(nameOfBranch.equals(currentBranch.getName())){
            throw new RuntimeException("No you can't delete the one you're inside");
        }
        File f = new File(RepositoryManager.PathOfBranchFolder + "/" + nameOfBranch);
        if(!f.delete())
            throw new RuntimeException("error in delete");
    }
}

