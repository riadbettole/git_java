package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.HashedFile;

import java.io.*;
import java.util.*;

public class FileManager {

    private static final Map<String, HashedFile> allFilesPresent = new HashMap<>();

    private static final Set<String> filesToIgnore = new HashSet<>();

    public static Map<String, HashedFile> get_all_files_present() {
        return allFilesPresent;
    }

    public static void find_all_files(String current) {

        //File current_directory = new File(InitialDirectory); not using that cuz of recursion
        File current_directory = new File(current);

        if (!current_directory.exists() || !current_directory.isDirectory()) {
            return;
        }

        File[] files = current_directory.listFiles();
        if (files == null) return;

        read_ignore_file();

        for (File file : files) {
            String filePath = file.getPath();
            if (check_if_file_is_ignored(file.getName())) {
                continue;
            }

            if (file.isDirectory()) {
                find_all_files(filePath);
            } else
                get_into_current_files(filePath);
        }

    }

    public static void get_into_current_files(String filePath) {
        HashedFile sf = new HashedFile(filePath);
        sf.sha1_the_file();
        allFilesPresent.put(filePath, sf);
    }

    public static boolean check_if_file_is_ignored(String filePathName) {
        return filesToIgnore.contains(filePathName);
    }

    public static void read_ignore_file() {
        String filePath = RepositoryManager.PathOfIgnoreFile;
        File ignoredFoldersFile = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(ignoredFoldersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                filesToIgnore.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear_current_all_files_present() {
        allFilesPresent.clear();
    }
}