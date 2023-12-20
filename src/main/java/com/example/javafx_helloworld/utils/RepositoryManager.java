package com.example.javafx_helloworld.utils;

import javafx.stage.DirectoryChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class RepositoryManager {

    private static String directoryPath;

    public static String PathOfRepository ;
    public static String PathOfIgnoreFile ;
    public static String PathOfZippedFolders ;
    public static String PathOfStagingFile ;

    public static void set_directory_path(String _directoryPath) {
        directoryPath = _directoryPath;
        PathOfRepository = directoryPath + "/.gitryad";
        PathOfIgnoreFile = directoryPath + "/.gitignoreryad";
        PathOfZippedFolders = directoryPath + "/.gitryad/ZippedFolders";
        PathOfStagingFile = directoryPath + "/.gitryad/ZippedFolders/staging";
    }
    public static String get_directory_path() { return directoryPath; }

    public static void create_repository_folder() {
        Path path = Paths.get(PathOfRepository);
        try {
            Files.createDirectories(path);
            Files.setAttribute(path, "dos:hidden", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void create_ignore_file() {
        Path path = Paths.get(PathOfIgnoreFile);
        try {
            Files.createFile(path);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                String file_to_hide = ".gitryad\n.gitignoreryad\n";
                writer.write(file_to_hide);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void create_folder_of_zipped_folders() {
        Path path = Paths.get(PathOfZippedFolders);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void create_recent_projects_file() {
        String userHome = System.getProperty("user.home");
        String relativePath = "Documents";
        Path documentsPath = Paths.get(userHome, relativePath);
        Path filePath = documentsPath.resolve("recent_folders_git");

        try {
            Files.createDirectories(filePath.getParent());
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean zipped_folder_doesnt_exist() {
        String filePath = PathOfZippedFolders;
        File file = new File(filePath);

        return !file.exists();
    }

    public static boolean recent_projects_file_doesnt_exist() {
        String userHome = System.getProperty("user.home");
        String relativePath = "Documents";
        Path documentsPath = Paths.get(userHome, relativePath);
        Path filePath = documentsPath.resolve("recent_folders_git");

        return !Files.exists(filePath);
    }


    public static boolean ignored_files_doesnt_exist() {
        String filePath = PathOfIgnoreFile;

        File file = new File(filePath);

        return !file.exists();
    }

    public static boolean repository_doesnt_exist() {
        String filePath = PathOfRepository;

        File file = new File(filePath);

        return !file.exists();
    }

    public static void files_and_folders_exist(){
        if (repository_doesnt_exist())
            create_repository_folder();

        if (ignored_files_doesnt_exist())
            create_ignore_file();

        if (zipped_folder_doesnt_exist())
            create_folder_of_zipped_folders();

        if(recent_projects_file_doesnt_exist())
            create_recent_projects_file();
    }

    public static void select_folder_of_project() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        directoryChooser.setInitialDirectory(new File("."));
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null)
            return;

        set_directory_path(selectedDirectory.getAbsolutePath());
        files_and_folders_exist();

    }

    public static void delete_this_repo() {
        File current_directory = new File(PathOfRepository);
        deleteFolder(current_directory);
        try{
        Files.deleteIfExists(Paths.get(PathOfRepository));
        Files.deleteIfExists(Paths.get(PathOfIgnoreFile));
        }catch(IOException e){
            throw new RuntimeException(e);
        }

        select_folder_of_project();
    }

    static void deleteFolder(File file) {
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }
}
