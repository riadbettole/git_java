package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.*;

public class FileManager {

    private static final Map<String, HashedFile> allFilesPresent = new HashMap<>();

    public static Map<String, HashedFile> get_all_present_files() {
        return allFilesPresent;
    }
    public static void clear_current_state_of_project() {
        allFilesPresent.clear();
    }

    public static void get_into_current_files(String filePath) {

        HashedFile sf = new HashedFile(filePath);
        sf.sha1_the_file();
        allFilesPresent.put(filePath, sf);
    }
    public static void get_all_present_files_in_directory(String current) {
        File currentProjectDirectory = new File(current);

        if (!currentProjectDirectory.exists() || !currentProjectDirectory.isDirectory()) {
            return;
        }

        File[] files = currentProjectDirectory.listFiles();
        if (files == null) return;

        Set<String> filesToIgnore = load_ignore_file();

        for (File file : files) {
            String filePath = file.getPath();
            if (check_if_file_is_ignored(filesToIgnore, file.getName())) {
                continue;
            }

            if (file.isDirectory()) {
                get_all_present_files_in_directory(filePath);
            } else
                get_into_current_files(filePath);
        }

    }

    public static boolean check_if_file_is_ignored(Set<String> filesToIgnore, String filePathName) {
        return filesToIgnore.contains(filePathName);
    }
    public static Set<String> load_ignore_file() {
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