package com.example.javafx_helloworld;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.DeflaterOutputStream;



public class StagingManager {

    static String PathOfZippedFolders = ".gitryad/ZippedFolders";

    public static void set_initial_path_Of_zipped_folders(String initialDirectory) {
        PathOfZippedFolders = initialDirectory + "/" + PathOfZippedFolders;
    }

    public static String convert_file_to_sha1(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(filePath));

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(fileBytes);
            byte[] hashBytes = sha1.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                hexString.append(String.format("%02x", hashByte));
            }

            return hexString.toString();
        } catch (IOException|NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    public static void compress_file_content_into_folder(String inputFileName, String outputFileName){
        try (FileInputStream in = new FileInputStream(inputFileName);
             BufferedInputStream inBuffered = new BufferedInputStream(in);
             FileOutputStream out = new FileOutputStream(outputFileName);
             DeflaterOutputStream zipped = new DeflaterOutputStream(out);
             BufferedOutputStream outBuffered = new BufferedOutputStream(zipped)){

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inBuffered.read(buffer)) != -1) {
                outBuffered.write(buffer, 0, bytesRead);
            }


        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }


    public static void create_zipped_file_folder(String wholePathForFolder){
        Path path = Paths.get(wholePathForFolder);
        try{
            Files.createDirectories(path);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static boolean zipped_file_folder_doesnt_exist(String firstTwoCharacters) {
        String filePath = PathOfZippedFolders +"/" + firstTwoCharacters;
        File file = new File(filePath);

        return !file.exists();
    }

    public static void update_staging_file(String filePath,String hashedName){
        // get and update old staging to make a new one
        HashMap<String, String> oldStaging = retrieve_staging();
        oldStaging.put(filePath,hashedName);

        //create and save state file
        try (ObjectOutputStream outStagingFile = new ObjectOutputStream(new FileOutputStream(PathOfZippedFolders + "/staging"))) {
            outStagingFile.writeObject(oldStaging);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> retrieve_staging(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PathOfZippedFolders + "/staging"))) {
            return (HashMap<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Return an empty map if the file doesn't exist (first run)
            return new HashMap<>();
        }
    }

//    public static void add_file_to_staging(ArrayList<String> filePaths){
    public static void add_file_to_staging(String filePath){
//        String filePath = f.getPath();
            String hashedName = convert_file_to_sha1(filePath);

            String firstTwoCharacters = hashedName.substring(0, 2);
            String hashedNameForFile = hashedName.substring(2);
            String wholePathForFolder = PathOfZippedFolders + "/" + firstTwoCharacters;
            String wholePathForFile = wholePathForFolder + "/" + hashedNameForFile;

            if(zipped_file_folder_doesnt_exist(firstTwoCharacters)) {
                create_zipped_file_folder(wholePathForFolder);
            }

            compress_file_content_into_folder(filePath,wholePathForFile);
            update_staging_file(filePath, hashedName);
            
    }
    
    public static void delete_file_from_staging(String filePath){
        // get and update old staging to make a new one
        HashMap<String, String> oldStaging = retrieve_staging();
        oldStaging.remove(filePath);

        //create and save state file
        try (ObjectOutputStream outStagingFile = new ObjectOutputStream(new FileOutputStream(PathOfZippedFolders + "/staging"))) {
            outStagingFile.writeObject(oldStaging);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
