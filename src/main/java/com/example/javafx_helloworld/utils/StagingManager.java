package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.enums.FileStateEnums;
import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.*;


public class StagingManager {

    public static boolean stagingAreaFileDoesNotExist() {
        String filePath = RepositoryManager.PathOfStagingAreaFile;

        File file = new File(filePath);

        return !file.exists();
    }

    /**Compress and save file to staging Area*/
    public static void addFileToStagingArea(HashedFile addedFileToStagingArea){
        HashMap<String, HashedFile> stagingArea;

        if(StagingManager.stagingAreaFileDoesNotExist()) {
            stagingArea = new HashMap<>();
        }else {
            stagingArea = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);
        }

        String zipPath = addedFileToStagingArea.get_zip_path();
        String wholePathForFile = addedFileToStagingArea.get_file_path();
        CompressionManager.compressFileContentIntoZippedFolder(wholePathForFile, zipPath);

        addedFileToStagingArea.setState(FileStateEnums.ADDED);
        stagingArea.put(wholePathForFile,addedFileToStagingArea);

        LoadingSavingManager.saveData(RepositoryManager.PathOfStagingAreaFile, stagingArea);
    }
    public static void removeFromStagingArea(HashedFile fileToRemoveFromStagingArea){
        HashMap<String, HashedFile> stagingArea = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);

        String filePathOfAddedFileToStagingArea = fileToRemoveFromStagingArea.get_file_path();

        stagingArea.remove(filePathOfAddedFileToStagingArea);

        LoadingSavingManager.saveData(RepositoryManager.PathOfStagingAreaFile, stagingArea);
    }
    public static void restoreFileFromStagingData(HashedFile fileToRestoreFromStagingArea) {
        String zipPath = fileToRestoreFromStagingArea.get_zip_path();
        String wholePathForFileToBeRestored = fileToRestoreFromStagingArea.get_file_path();

        CompressionManager.uncompressFileContentIntoItsPlace(zipPath, wholePathForFileToBeRestored);
    }

    public static HashMap<FileStateEnums, ArrayList<HashedFile>> detectChangesInStagingData(){
        Map<String, HashedFile> allPresentFilesInDirectory = FileManager.getAllPresentFilesOfCurrentDirectoryInHashedForm();
        Map<String, HashedFile> stagingArea;

        if(StagingManager.stagingAreaFileDoesNotExist()) {
            stagingArea = new HashMap<>();
        }else {
            stagingArea = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);
        }

        HashMap<FileStateEnums, ArrayList<HashedFile>> allChangesInStagedArea = new HashMap<>();

        stagingArea.forEach((stagedFilePath, hashedAddedFile)->{
            boolean doesTheFileStillExist = allPresentFilesInDirectory.containsKey(stagedFilePath);

            FileStateEnums state = defineStateOfCurrentFile(hashedAddedFile, doesTheFileStillExist, allChangesInStagedArea, allPresentFilesInDirectory);
            if (state == null) {
                return;
            }

            hashedAddedFile.setState(state);
            allChangesInStagedArea.computeIfAbsent(state, k -> new ArrayList<>()).add(hashedAddedFile);

            allPresentFilesInDirectory.remove(stagedFilePath); //gets removed to not get detected in the next iteration
        });

        //rest of the files that hasn't been removed go to unstaged area
        allPresentFilesInDirectory.forEach((filePath, hashedFile) -> {
            allChangesInStagedArea.computeIfAbsent(FileStateEnums.UNADDED, k -> new ArrayList<>()).add(hashedFile);
            hashedFile.setState(FileStateEnums.UNADDED);
        });

        return allChangesInStagedArea;
    }
    private static FileStateEnums defineStateOfCurrentFile(HashedFile hashedAddedFile, boolean fileExist, HashMap<FileStateEnums, ArrayList<HashedFile>> allChangesInStagedArea, Map<String, HashedFile> allPresentFilesInDirectory) {
        FileStateEnums state ;

        if(!fileExist){
            hashedAddedFile.setState(FileStateEnums.DELETED);
            allChangesInStagedArea.computeIfAbsent(FileStateEnums.DELETED, k -> new ArrayList<>()).add(hashedAddedFile);
            return null;
        }
        //only three logical choices if the file is not deleted
        if(fileIsModified(hashedAddedFile, allPresentFilesInDirectory))
            state = FileStateEnums.MODIFIED;
        else if(!hashedAddedFile.isCommited()) //file is not commited then its added
            state = FileStateEnums.ADDED;
        else
            state = FileStateEnums.COMITED;

        return state;
    }
    private static boolean fileIsModified(HashedFile hashedFile, Map<String, HashedFile> allPresentFiles) {
        String filePath = hashedFile.get_file_path();
        return !hashedFile.get_hashed_name()
                .equals( allPresentFiles.get(filePath).get_hashed_name() );
    }

    public static void updateStagedFilesToCommitted(){
        HashMap<String,HashedFile> stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);

        stagingData.forEach((pathStagedFile, stagedFile)->{
            if(stagingData.get(pathStagedFile).get_state() == FileStateEnums.ADDED){
                stagedFile.setCommited(true);
            }
        });

        LoadingSavingManager.saveData(RepositoryManager.PathOfStagingAreaFile, stagingData);
    }
}

