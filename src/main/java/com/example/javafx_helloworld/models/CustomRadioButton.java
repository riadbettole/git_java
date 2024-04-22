package com.example.javafx_helloworld.models;

import javafx.scene.control.RadioButton;

public class CustomRadioButton<T> extends RadioButton {
    private final T item;

    public CustomRadioButton(T item, String text, String color) {
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
