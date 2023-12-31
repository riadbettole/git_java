package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Commit;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class BranchManager {
    static public HashMap<String, Branch> Branches;

    static public void checkout_commit(Commit x){
        File f = new File(RepositoryManager.PathOfRepository);
        delete_directory(f);
        load_files_of_commit_X_to_place(x);
    }

    private static void delete_directory(File current_directory) {
        File [] files = current_directory.listFiles();
        assert files != null;
        Set<String> filesToIgnore = FileManager.load_ignore_file();
        for (File subfile : files) {
            if(FileManager.check_if_file_is_ignored(filesToIgnore, subfile.getName()))
                continue;

            if (subfile.isDirectory()) {
                delete_directory(subfile);
            }

            if(subfile.delete()) return;
        }
    }

    public static void load_files_of_commit_X_to_place(Commit x){
        String PathOfZippedStaging = x.getHashedZippedCommitedStagingPath();
        File f = new File(RepositoryManager.PathOfStagingFile);
        if(!f.delete()) return;

        CompressionManager.uncompress_file_content_into_its_place(PathOfZippedStaging, RepositoryManager.PathOfStagingFile);
    }

    public static HashMap<String, Branch> getBranches() {
        return Branches;
    }
    public static void setBranches(HashMap<String, Branch> branches) {
        Branches = branches;
    }
}

