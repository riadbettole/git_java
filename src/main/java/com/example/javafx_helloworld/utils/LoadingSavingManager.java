package com.example.javafx_helloworld.utils;

import java.io.*;

public class LoadingSavingManager {
    @SuppressWarnings("unchecked")
    static public <T> T load_item(String Path){
        try(ObjectInputStream e = new ObjectInputStream(new FileInputStream(Path))){
            return (T) e.readObject();
        }catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <U> void saveData(String filePath, U data) {
        try (ObjectOutputStream outStagingFile = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outStagingFile.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
