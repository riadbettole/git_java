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
    static final private String FIRST_BRANCH = "Master";
    static final private String NAME_OF_CURRENT_BRANCH_FILE = "currentBranch";

    static public ArrayList<Branch> getAllAvailableBranches(){
        ArrayList<Branch> allBranches = new ArrayList<>();
        File f = new File(RepositoryManager.PathOfBranchFolder);
        File[] files = f.listFiles();
        if (files == null) {
            throw new RuntimeException("No files");
        }

        for(File subfile : files){
            String name = subfile.getName();
            if(!name.equals(NAME_OF_CURRENT_BRANCH_FILE))
                allBranches.add(LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + name));
        }

        return allBranches;
    }

    static public void saveWhichBranchIsTheCurrent(String Name){
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/" + NAME_OF_CURRENT_BRANCH_FILE, Name);
    }
    static public String loadWhichBranchIsTheCurrent(){
        return LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + NAME_OF_CURRENT_BRANCH_FILE);
    }

    public static void setAndLoadCurrentBranch(){
        if(!branchExist(FIRST_BRANCH))
            saveWhichBranchIsTheCurrent(FIRST_BRANCH);
        loadBranch(loadWhichBranchIsTheCurrent());
    }

    static public void loadBranch(String name){
        saveWhichBranchIsTheCurrent(name);
        if(branchExist(name)){
            currentBranch = LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + name);
        }else{
            currentBranch = new Branch(FIRST_BRANCH);
            addOrSaveBranch(FIRST_BRANCH);
        }
    }

    static public void checkoutCommit(Commit commit){
        loadFilesOfCommitXToPlace(commit);
    }

    public static void loadFilesOfCommitXToPlace(Commit commitToPlace){
        String pathOfZippedStaging = commitToPlace.getZippedCommitedStagingPath();
        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);

        File f = new File(RepositoryManager.PathOfStagingAreaFile);
        if(!f.delete()) return;

        File currentDirectory = new File(RepositoryManager.PathOfRepository);
        deleteAllFiles(currentDirectory.getParentFile());

        CompressionManager.uncompressFileContentIntoItsPlace(pathOfZippedStaging, RepositoryManager.PathOfStagingAreaFile);
        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);
        stagingData.forEach((path,hashedFile)-> CompressionManager.uncompressFileContentIntoItsPlace(hashedFile.get_zip_path(), hashedFile.get_file_path()) );
    }

    private static void deleteAllFiles(File current_directory) {
        File [] files = current_directory.listFiles();
        assert files != null;
        Set<String> filesToIgnore = FileManager.loadAllFilesThatHasToBeIgnored();
        for (File subfile : files) {
            if(!stagingData.containsKey(subfile.getPath()) | FileManager.checkIfFileHasToBeIgnored(filesToIgnore, subfile.getName()))
                continue;

            if (subfile.isDirectory()) {
                deleteAllFiles(subfile);
            }

            if(!subfile.delete())
                throw new RuntimeException("error in delete");
        }
    }

    public static void addNewCommit(String message) {
        HashedFile hashedStagingFile = new HashedFile(RepositoryManager.PathOfStagingAreaFile);

        String zipPath = hashedStagingFile.get_zip_path();
        String wholePathForFile = hashedStagingFile.get_file_path();
        CompressionManager.compressFileContentIntoZippedFolder(wholePathForFile, zipPath);

        Commit newCommit = new Commit(message, "ryad", hashedStagingFile.get_zip_path());
        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
        allCommits.add(newCommit);

        StagingManager.updateStagedFilesToCommitted();

        addOrSaveBranch(currentBranch.getName());
        saveWhichBranchIsTheCurrent(currentBranch.getName());
    }

    static public boolean branchExist(String name){
        File file = new File(RepositoryManager.PathOfBranchFolder + "/" + name);
        return file.exists();
    }

    static public Branch getCurrentBranch() {
        return currentBranch;
    }

    static public void addOrSaveBranch(String nameOfBranch){
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

