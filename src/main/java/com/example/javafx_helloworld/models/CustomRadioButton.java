package com.example.javafx_helloworld.models;

import javafx.scene.control.RadioButton;

public class CustomRadioButton<T> extends RadioButton {
    private final T item;

    public CustomRadioButton(T item, String text, String color) {
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
