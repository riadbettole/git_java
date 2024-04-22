package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.*;

public class FileManager {

    private static final Map<String, HashedFile> ALL_PRESENT_FILES_OF_CURRENT_DIRECTORY_IN_HASHED_FORM = new HashMap<>();

    public static Map<String, HashedFile> getAllPresentFilesOfCurrentDirectoryInHashedForm() {
        return ALL_PRESENT_FILES_OF_CURRENT_DIRECTORY_IN_HASHED_FORM;
    }
    public static void clearCurrentStateOfProject() {
        ALL_PRESENT_FILES_OF_CURRENT_DIRECTORY_IN_HASHED_FORM.clear();
    }

    public static void detectAllPresentFilesInThisDirectory(String current) {
        File currentDirectory = new File(current);

        if (!currentDirectory.exists() || !currentDirectory.isDirectory()) {
            return;
        }

        File[] filesOfCurrentDirectory = currentDirectory.listFiles();
        if (filesOfCurrentDirectory == null) return;

        Set<String> filesToIgnore = loadAllFilesThatHasToBeIgnored();

        hashAndStoreValidFiles(filesOfCurrentDirectory, filesToIgnore);

    }
    private static void hashAndStoreValidFiles(File[] filesOfCurrentDirectory, Set<String> filesToIgnore) {
        for (File file : filesOfCurrentDirectory) {
            String filePath = file.getPath();
            if (checkIfFileHasToBeIgnored(filesToIgnore, file.getName())) {
                continue;
            }

            if (file.isDirectory()) {
                detectAllPresentFilesInThisDirectory(filePath);
            } else {
                putIntoAllPresentFilesHashMap(filePath);
            }
        }
    }
    public static void putIntoAllPresentFilesHashMap(String filePath) {
        HashedFile sf = new HashedFile(filePath);
        sf.sha1TheFile();
        ALL_PRESENT_FILES_OF_CURRENT_DIRECTORY_IN_HASHED_FORM.put(filePath, sf);
    }

    public static boolean checkIfFileHasToBeIgnored(Set<String> filesToIgnore, String filePathName) {
        return filesToIgnore.contains(filePathName);
    }
    public static Set<String> loadAllFilesThatHasToBeIgnored() {
        Set<String> filesToIgnore = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(RepositoryManager.PathOfIgnoreFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                filesToIgnore.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filesToIgnore;
    }

}