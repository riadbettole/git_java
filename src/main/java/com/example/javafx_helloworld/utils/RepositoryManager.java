package com.example.javafx_helloworld.utils;

import javafx.stage.DirectoryChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryManager {

    private static String directoryPath;

    public static String PathOfRepository ;
    public static String PathOfIgnoreFile ;
    public static String PathOfZippedFolders ;
    public static String PathOfStagingAreaFile;
    public static String PathOfBranchFolder ;

    public static String RemoteUrlRepository ;
    public static String KeyRepository ;

    public static void setDirectoryPath(String _directoryPath) {
        directoryPath = _directoryPath;
        PathOfRepository    = directoryPath + "/.gitryad"               ;
        PathOfIgnoreFile    = directoryPath + "/.gitignoreryad"         ;
        PathOfZippedFolders = directoryPath + "/.gitryad/ZippedFolders" ;
        PathOfStagingAreaFile = directoryPath + "/.gitryad/staging"       ;
        PathOfBranchFolder  = directoryPath + "/.gitryad/BranchFolder"  ;
    }
    public static String getDirectoryPath() { return directoryPath; }

    public static void selectFolderOfRepository() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory");

        directoryChooser.setInitialDirectory(new File("."));
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null) {
            return;
        }

        setDirectoryPath(selectedDirectory.getAbsolutePath());
        initializeRepositoryFoldersAndFiles();

    }
    public static void initializeRepositoryFoldersAndFiles(){
        if (repositoryDoesntExist())
            initializeRepositoryFolder();

        if (ignoredFilesDoesntExist())
            initializeIgnoreFile();

        if (zippedFolderDoesntExist())
            initializeFolderOfZippedFolders();

        if(recentProjectsFileDoesntExist())
            initializeRecentProjectsFile();

        if(branchFolderDoesntExist())
            initializeBranchesFolder();
    }

    private static void initializeRepositoryFolder() {
        Path path = Paths.get(PathOfRepository);
        try {
            Files.createDirectories(path);
            Files.setAttribute(path, "dos:hidden", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void initializeIgnoreFile() {

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
    private static void initializeRecentProjectsFile() {

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
    private static void initializeFolderOfZippedFolders() {

        Path path = Paths.get(PathOfZippedFolders);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void initializeBranchesFolder(){
        File f = new File(PathOfBranchFolder);
        try{
            Files.createDirectories(f.toPath());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static boolean recentProjectsFileDoesntExist() {

        String userHome = System.getProperty("user.home");
        String relativePath = "Documents";
        Path documentsPath = Paths.get(userHome, relativePath);
        Path filePath = documentsPath.resolve("recent_folders_git");

        return !Files.exists(filePath);
    }
    private static boolean repositoryDoesntExist() {

        String filePath = PathOfRepository;

        File file = new File(filePath);

        return !file.exists();
    }
    private static boolean ignoredFilesDoesntExist() {

        String filePath = PathOfIgnoreFile;

        File file = new File(filePath);

        return !file.exists();
    }
    private static boolean zippedFolderDoesntExist() {
        String filePath = PathOfZippedFolders;
        File file = new File(filePath);

        return !file.exists();
    }
    private static boolean branchFolderDoesntExist() {
        String filePath = PathOfBranchFolder;
        File file = new File(filePath);
        return !file.exists();
    }

    public static boolean repositoryDoesntExistAtThisPath(String directoryPath) {
        String filePath = directoryPath + "/.gitryad";

        File file = new File(filePath);

        return !file.exists();
    }

    public static void deleteThisRepository() {
        File current_directory = new File(PathOfRepository);
        try{
            deleteDirectory(current_directory);
            Files.deleteIfExists(Paths.get(PathOfRepository));
            Files.deleteIfExists(Paths.get(PathOfIgnoreFile));
        }catch( IOException e){
            throw new RuntimeException(e);
        }
    }
    private static void deleteDirectory(File current_directory) {
        File [] files = current_directory.listFiles();
        assert files != null;
        for (File subfile : files) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            if(subfile.delete()) {
                return;
            }
        }
    }

    public static void setKeyAndUrl(String url, String key) {
        RemoteUrlRepository = url;
        KeyRepository = key;
    }
}
