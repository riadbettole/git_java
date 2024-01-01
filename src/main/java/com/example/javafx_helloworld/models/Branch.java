package com.example.javafx_helloworld.models;

import com.example.javafx_helloworld.models.Commit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class Branch implements Serializable {
    LinkedList<Commit> allCommits;
    String name;
    LocalDateTime currentTime;

    public Branch(String name){
        this.name = name;
        this.allCommits = new LinkedList<>();
        this.currentTime = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public LinkedList<Commit> getAllCommits() {
        return allCommits;
    }
}
