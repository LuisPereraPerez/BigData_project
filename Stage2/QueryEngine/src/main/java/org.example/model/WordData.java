package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class WordData {
    private int totalOccurrences;
    private final List<WordPosition> positions;

    public WordData(int initialOccurrences) {
        this.totalOccurrences = initialOccurrences;
        this.positions = new ArrayList<>();
    }

    public void addPosition(WordPosition position) {
        positions.add(position);
    }

    public void incrementOccurrences(int occurrences) {
        this.totalOccurrences += occurrences;
    }

    public int getTotalOccurrences() {
        return totalOccurrences;
    }

    public void incrementTotalOccurrences(int occurrences) {
        this.totalOccurrences += occurrences;
    }

    public void setTotalOccurrences(int totalOccurrences) {
        this.totalOccurrences = totalOccurrences;
    }

    public List<WordPosition> getPositions() {
        return positions;
    }
}
