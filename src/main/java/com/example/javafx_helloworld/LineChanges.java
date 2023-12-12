package com.example.javafx_helloworld;

public class LineChanges implements Comparable<LineChanges> {
    String line;
    LineState color;
    int index;

    public LineChanges(String line, LineState color, int index) {
        this.line = line;
        this.color = color;
        this.index = index;
    }

    public String getLine() {
        return line;
    }

    public LineState getColor() {
        return color;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(LineChanges other) {
        return Integer.compare(this.index, other.index);
    }
}
