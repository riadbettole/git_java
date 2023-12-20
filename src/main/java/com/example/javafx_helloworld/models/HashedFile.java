package com.example.javafx_helloworld.models;

import com.example.javafx_helloworld.utils.RepositoryManager;
import com.example.javafx_helloworld.enums.FileStateEnums;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashedFile implements Serializable {
    String parent;
    String filePath;
    String zipPath;
    String hashedName;
    FileStateEnums state;

    public HashedFile(String filePath) {
        this.filePath = filePath;
    }

    public void sha1_the_file() {
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(filePath));

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(fileBytes);
            byte[] hashBytes = sha1.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hexString.append(String.format("%02x", hashByte));
            }

            set_hashed_name(hexString.toString());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    public void setup_file_parent_and_zip_path(){
        String firstTwoCharacters = hashedName.substring(0, 2);
        String hashedNameForFile = hashedName.substring(2);

        set_parent_path(RepositoryManager.PathOfZippedFolders + "/" + firstTwoCharacters);
        set_zip_path(parent + "/" + hashedNameForFile);
    }

    public String get_hashed_name() {
        return hashedName;
    }

    public void set_hashed_name(String hashedName) {
        this.hashedName = hashedName;
        setup_file_parent_and_zip_path();
    }

    public String get_zip_path() {
        return zipPath;
    }

    public void set_zip_path(String zipPath) {
        this.zipPath = zipPath;
    }

    public String get_file_path() {
        return filePath;
    }
    public String get_parent_path() {
        return parent;
    }
    public void set_parent_path(String parentPath) {
        this.parent = parentPath;
    }

    public FileStateEnums get_state() {
        return state;
    }

    public void setState(FileStateEnums state) {
        this.state = state;
    }
}
