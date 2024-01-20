package com.example.javafx_helloworld.models;

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
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Commit> getAllCommits() {
        return allCommits;
    }

}
