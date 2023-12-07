package com.example.javafx_helloworld;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HandleRepository {

    private static String directoryPath;

    private static final String PathOfRepository = "/.gitryad";
    private static final String PathOfIgnoreFile = "/.gitignoreryad";

    public static void set_directory_path(String directoryPath) {
        HandleRepository.directoryPath = directoryPath;
    }

    public static void create_repository_Folder() {
        Path path = Paths.get(directoryPath + PathOfRepository);
        try {
            Files.createDirectories(path);
            Files.setAttribute(path, "dos:hidden", true);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void create_ignore_file() {
        Path path = Paths.get(directoryPath+ PathOfIgnoreFile);
        try{
            Files.createFile(path);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                String file_to_hide = ".gitryad\n";
                writer.write(file_to_hide);
            }
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static boolean ignored_files_doesnt_exist() {
        String filePath = directoryPath + PathOfIgnoreFile;

        File file = new File(filePath);

        return !file.exists();
    }

    public static boolean repository_doesnt_exist() {
        String filePath = directoryPath + PathOfRepository;

        File file = new File(filePath);

        return !file.exists();
    }
}
