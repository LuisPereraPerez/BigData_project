package org.example.model;

public class WordPosition {
    private final int line;
    private final int occurrences;

    public WordPosition(int line, int occurrences) {
        this.line = line;
        this.occurrences = occurrences;
    }

    public int getLine() {
        return line;
    }

    public int getOccurrences() {
        return occurrences;
    }
}
