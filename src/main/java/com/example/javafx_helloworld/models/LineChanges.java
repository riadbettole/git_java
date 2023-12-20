package com.example.javafx_helloworld.models;

import com.example.javafx_helloworld.enums.LineState;

public class LineChanges implements Comparable<LineChanges> {
    String line;
    LineState color;
    int index;

    public LineChanges(String line, LineState color, int index) {
        this.line = line;
        this.color = color;
        this.index = index;
    }

    public LineState getColor() {
        return color;
    }

    @Override
    public int compareTo(LineChanges other) {
        return Integer.compare(this.index, other.index);
    }

    public String getLine() {
        return line;
    }

}
