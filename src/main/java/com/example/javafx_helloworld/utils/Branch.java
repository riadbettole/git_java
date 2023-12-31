package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Commit;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class Branch {
    LinkedList<Commit> allCommits;
    String name;
    LocalDateTime currentTime;

}
