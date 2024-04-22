package com.example.javafx_helloworld.models;

import javafx.scene.control.CheckBox;

public class CustomCheckBox<T> extends CheckBox {
    private final T item;

    public CustomCheckBox(T item,String text, String color) {
        super(text);
        this.item = item;
        updateStyle(color);
    }

    public T getItem() {
        return item;
    }

    public void updateStyle(String color) {
        setStyle("-fx-text-fill: "+color+";");
    }
}
