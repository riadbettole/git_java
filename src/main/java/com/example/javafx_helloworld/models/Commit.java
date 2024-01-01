package com.example.javafx_helloworld.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Commit implements Serializable {
    String message;
    String commiter;
    String hashedZippedCommitedStagingPath;
    LocalDateTime currentTime;

    public Commit(String message, String commiter, String hashedZippedCommitedStagingPath) {
        this.message = message;
        this.commiter = commiter;
        this.hashedZippedCommitedStagingPath = hashedZippedCommitedStagingPath;
        this.currentTime = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public String get_zipped_commited_stagingPath() {
        return hashedZippedCommitedStagingPath;
    }
}

