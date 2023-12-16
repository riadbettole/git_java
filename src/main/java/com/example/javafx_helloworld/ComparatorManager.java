package com.example.javafx_helloworld;

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

    public static void set_old_file_path(String _o){
        oldFilePath = _o;
    }

    public static void set_new_file_path(String _n){
        newFilePath = _n;
    }

    public static void to_map_files(){
            oldFileMap = process_file(oldFilePath);
            newFileMap = process_file(newFilePath);
    }

    private static Map<String, Integer> process_file(String filePath){
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


    private static final ArrayList<LineChanges> currentLinesChanges = new ArrayList<>();

    public static void compare( ){
        currentLinesChanges.clear();

        oldFileMap.forEach((line, index)->{
            boolean isLineDeletedFromNewFile = !newFileMap.containsKey(line);
            if(isLineDeletedFromNewFile){
                LineChanges change = new LineChanges(line, LineState.DELETED, index);
                currentLinesChanges.add(change);
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
            currentLinesChanges.add(change);
        });
    }

    public static ArrayList<LineChanges> get_current_changes_lines() {
        return currentLinesChanges;
    }
}

