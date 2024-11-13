package com.example.model;

public class Position {
    private int line;
    private int wordIndex;

    public Position(int line, int wordIndex) {
        this.line = line;
        this.wordIndex = wordIndex;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getWordIndex() {
        return wordIndex;
    }

    public void setWordIndex(int wordIndex) {
        this.wordIndex = wordIndex;
    }
}
