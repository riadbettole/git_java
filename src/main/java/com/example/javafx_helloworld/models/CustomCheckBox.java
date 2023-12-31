package com.example.javafx_helloworld.models;

import javafx.scene.control.CheckBox;

public class CustomCheckBox<T> extends CheckBox {
    private final T item;

    public CustomCheckBox(T item,String text, String color) {
        super(text);
        this.item = item;
        update_style(color);
    }

    public T get_item() {
        return item;
    }

    public void update_style(String color) {
        setStyle("-fx-text-fill: "+color+";");
    }
}
