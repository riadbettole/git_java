package com.example.javafx_helloworld.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CompressionManager {
    public static void compressFileContentIntoZippedFolder(String inputFileName, String outputFileName){
        create_file_directories_if_not_present(outputFileName);

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
    public static void uncompressFileContentIntoItsPlace(String fileToUnzip, String whereToUnzip){
        create_file_directories_if_not_present(whereToUnzip);

        try (FileInputStream in = new FileInputStream(fileToUnzip);
             BufferedInputStream inBuffered = new BufferedInputStream(in);
             InflaterInputStream unzipped = new InflaterInputStream(inBuffered);
             FileOutputStream out = new FileOutputStream(whereToUnzip);
             BufferedOutputStream outBuffered = new BufferedOutputStream(out)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = unzipped.read(buffer)) != -1) {
                outBuffered.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File unCompressFileContent(String fileToUnzip){
        try (FileInputStream in = new FileInputStream(fileToUnzip);
             BufferedInputStream inBuffered = new BufferedInputStream(in);
             InflaterInputStream unzipped = new InflaterInputStream(inBuffered)) {

            File tempFile = File.createTempFile("unzipped", ".tmp");

            try (FileOutputStream out = new FileOutputStream(tempFile);
                 BufferedOutputStream outBuffered = new BufferedOutputStream(out)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = unzipped.read(buffer)) != -1) {
                    outBuffered.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error while uncompressing file: " + e.getMessage());
        }
    }

    private static void create_file_directories_if_not_present(String outputFileName){
        File f = new File(outputFileName);
        try{
            File parentDir = f.getParentFile();
            if(!parentDir.exists()){
                Files.createDirectories(parentDir.toPath());
            }}catch (IOException e){
            throw new RuntimeException();
        }
    }
}
