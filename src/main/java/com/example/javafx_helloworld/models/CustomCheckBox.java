package com.example.javafx_helloworld.models;

import com.example.javafx_helloworld.enums.FileStateEnums;
import javafx.scene.control.CheckBox;

public class CustomCheckBox extends CheckBox {
    private final HashedFile hashedFile;

    public CustomCheckBox(HashedFile hashedFile) {
        super(hashedFile.get_file_path());
        this.hashedFile = hashedFile;
        update_style();
    }

    public HashedFile get_hashed_file() {
        return hashedFile;
    }

    public void update_style() {
        FileStateEnums state = hashedFile.get_state();
        if (state == FileStateEnums.ADDED)
            setStyle("-fx-text-fill: green;");
        else if (state == FileStateEnums.UNADDED)
            setStyle("-fx-text-fill: red;");
    }
}
