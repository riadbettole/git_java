package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.enums.LineState;
import com.example.javafx_helloworld.models.LineChanges;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComparatorManager {
    static String oldFilePath;
    static String newFilePath;
    static Map<String, Integer> oldFileMap = new HashMap<>();
    static Map<String, Integer> newFileMap = new HashMap<>();

    public static void setOldFilePath(String _o){
        oldFilePath = _o;
    }

    public static void setNewFilePath(String _n){
        newFilePath = _n;
    }

    public static void toMapFiles(){
            oldFileMap = processFile(oldFilePath);
            newFileMap = processFile(newFilePath);
    }

    private static Map<String, Integer> processFile(String filePath){
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath)) ) {
            String line = reader.readLine();
            int index = 1;

            Map<String, Integer> lineMap = new HashMap<>();

            while (line != null) {
                lineMap.put(line, index);
                index++;
                line = reader.readLine();
            }

            return lineMap;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    private static final ArrayList<LineChanges> CURRENT_LINES_CHANGES = new ArrayList<>();

    public static void compare( ){
        CURRENT_LINES_CHANGES.clear();

        oldFileMap.forEach((line, index)->{
            boolean isLineDeletedFromNewFile = !newFileMap.containsKey(line);
            if(isLineDeletedFromNewFile){
                LineChanges change = new LineChanges(line, LineState.DELETED, index);
                CURRENT_LINES_CHANGES.add(change);
            }
        });

        newFileMap.forEach((line, index) -> {
            boolean isLineNotPresentInOldFile = !oldFileMap.containsKey(line);

            LineChanges change;
            if (isLineNotPresentInOldFile && (!line.isEmpty() && !line.isBlank())) {
                change = new LineChanges(line, LineState.ADDED, index);
            } else{
                change = new LineChanges(line, LineState.PREVIOUS, index);
            }
            CURRENT_LINES_CHANGES.add(change);
        });
    }

    public static ArrayList<LineChanges> get_current_changes_lines() {
        return CURRENT_LINES_CHANGES;
    }
}

