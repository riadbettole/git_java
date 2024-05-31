package com.example.javafx_helloworld.utils;

import com.example.javafx_helloworld.models.Branch;
import com.example.javafx_helloworld.models.Commit;
import com.example.javafx_helloworld.models.HashedFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BranchManager {
    static HashMap<String, HashedFile> stagingData;
    static private Branch currentBranch;
    static final private String FIRST_BRANCH = "Master";
    static final private String NAME_OF_CURRENT_BRANCH_FILE = "currentBranch";

    static public ArrayList<Branch> getAllAvailableBranches(){
        ArrayList<Branch> allBranches = new ArrayList<>();
        File f = new File(RepositoryManager.PathOfBranchFolder);
        File[] files = f.listFiles();
        if (files == null) {
            throw new RuntimeException("No files");
        }

        for(File subfile : files){
            String name = subfile.getName();
            if(!name.equals(NAME_OF_CURRENT_BRANCH_FILE))
                allBranches.add(LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + name));
        }

        return allBranches;
    }

    static public String loadWhichBranchIsTheCurrent(){
        return LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + NAME_OF_CURRENT_BRANCH_FILE);
    }
    static public void saveWhichBranchIsTheCurrent(String Name){
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/" + NAME_OF_CURRENT_BRANCH_FILE, Name);
    }
    public static void setAndLoadCurrentBranch(){
        if(!branchExist(FIRST_BRANCH))
            saveWhichBranchIsTheCurrent(FIRST_BRANCH);
        loadBranch(loadWhichBranchIsTheCurrent());
    }
    static public void loadBranch(String name){
        saveWhichBranchIsTheCurrent(name);
        if(branchExist(name)){
            currentBranch = LoadingSavingManager.loadItem(RepositoryManager.PathOfBranchFolder + "/" + name);
        }else{
            currentBranch = new Branch(FIRST_BRANCH);
            addOrSaveBranch(FIRST_BRANCH);
        }
    }

    static public void checkoutCommit(Commit commit){
        loadFilesOfCommitXToPlace(commit);
    }
    public static void loadFilesOfCommitXToPlace(Commit commitToPlace){
        String pathOfZippedStaging = commitToPlace.getZippedCommitedStagingPath();
        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);

        File f = new File(RepositoryManager.PathOfStagingAreaFile);
        if(!f.delete()) return;

        File currentDirectory = new File(RepositoryManager.PathOfRepository);
        deleteAllFiles(currentDirectory.getParentFile());

        CompressionManager.uncompressFileContentIntoItsPlace(pathOfZippedStaging, RepositoryManager.PathOfStagingAreaFile);
        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);
        stagingData.forEach((path,hashedFile)-> CompressionManager.uncompressFileContentIntoItsPlace(hashedFile.get_zip_path(), hashedFile.get_file_path()) );
    }
    public static void addNewCommit(String message) {
        HashedFile hashedStagingFile = new HashedFile(RepositoryManager.PathOfStagingAreaFile);

        String zipPath = hashedStagingFile.get_zip_path();
        String wholePathForFile = hashedStagingFile.get_file_path();
        CompressionManager.compressFileContentIntoZippedFolder(wholePathForFile, zipPath);

        Commit newCommit = new Commit(message, "ryad", hashedStagingFile.get_zip_path());
        LinkedList<Commit> allCommits = currentBranch.getAllCommits();
        allCommits.add(newCommit);

        StagingManager.updateStagedFilesToCommitted();

        addOrSaveBranch(currentBranch.getName());
        saveWhichBranchIsTheCurrent(currentBranch.getName());
    }

    static public boolean branchExist(String name){
        File file = new File(RepositoryManager.PathOfBranchFolder + "/" + name);
        return file.exists();
    }
    static public Branch getCurrentBranch() {
        return currentBranch;
    }
    static public void addOrSaveBranch(String nameOfBranch){
        Branch branch = currentBranch;
        branch.setName(nameOfBranch);
        LoadingSavingManager.saveData(RepositoryManager.PathOfBranchFolder + "/"+ nameOfBranch , branch);
    }
    static public void delete_branch(String nameOfBranch){
        if(nameOfBranch.equals(currentBranch.getName())){
            throw new RuntimeException("No you can't delete the one you're inside");
        }
        File f = new File(RepositoryManager.PathOfBranchFolder + "/" + nameOfBranch);
        if(!f.delete())
            throw new RuntimeException("error in delete");
    }

    public static void pushBranch(){
        List<Commit> allCommits = currentBranch.getAllCommits();
        Commit commitToPlace = allCommits.getLast();
//        System.out.println(commit.getMessage());

        String pathOfZippedStaging = commitToPlace.getZippedCommitedStagingPath();
        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);

//        CompressionManager.uncompressFileContentIntoItsPlace(pathOfZippedStaging, RepositoryManager.PathOfStagingAreaFile);
//        stagingData = LoadingSavingManager.loadItem(RepositoryManager.PathOfStagingAreaFile);
//        stagingData.forEach((path,hashedFile)-> CompressionManager.uncompressFileContentIntoItsPlace(hashedFile.get_zip_path(), hashedFile.get_file_path()) );
        stagingData.forEach((path,hashedFile)-> x(hashedFile.get_zip_path(), hashedFile.get_file_path(),hashedFile.isCommited()));
    }

    private static void x(String zipPath, String path, boolean isCommited){
        if(!isCommited){
            return;
        }

        String staticPart = RepositoryManager.PathOfRepository.split("/")[0];
        System.out.println(staticPart);
        int staticPartEndIndex = path.indexOf(staticPart) + staticPart.length();

        String pathFinal = path.substring(staticPartEndIndex+1);

        System.out.println(pathFinal);
        File f = CompressionManager.unCompressFileContent(zipPath);
        pushFile(f, pathFinal);
    }

    public static void pushFile(File file, String path){
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            String url = "http://localhost:8080/upload/";

            HttpPost uploadFile = new HttpPost(url);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", new FileBody(file));

            builder.addPart("url", new StringBody(RepositoryManager.RemoteUrlRepository, StandardCharsets.UTF_8));
            builder.addPart("key", new StringBody(RepositoryManager.KeyRepository, StandardCharsets.UTF_8));

            builder.addPart("filePath", new StringBody(path, StandardCharsets.UTF_8));
            builder.addPart("branchName", new StringBody(getCurrentBranch().getName(), StandardCharsets.UTF_8));

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            HttpResponse response = httpClient.execute(uploadFile);

            System.out.println("Response Status: " + response.getStatusLine().getStatusCode());

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private static void deleteAllFiles(File current_directory) {
        File [] files = current_directory.listFiles();
        assert files != null;
        Set<String> filesToIgnore = FileManager.loadAllFilesThatHasToBeIgnored();
        for (File subfile : files) {
            if(!stagingData.containsKey(subfile.getPath()) | FileManager.checkIfFileHasToBeIgnored(filesToIgnore, subfile.getName()))
                continue;

            if (subfile.isDirectory()) {
                deleteAllFiles(subfile);
            }

            if(!subfile.delete())
                throw new RuntimeException("error in delete");
        }
    }
}

