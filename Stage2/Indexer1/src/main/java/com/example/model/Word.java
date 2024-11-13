package com.example.model;

import java.util.Map;

public class Word {

    private String word;
    private Map<String, BookAllocation> allocations;
    private int total;

    // Constructor
    public Word(String word, Map<String, BookAllocation> allocations, int total) {
        this.word = word;
        this.allocations = allocations;
        this.total = total;
    }

    // Getters and Setters
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Map<String, BookAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(Map<String, BookAllocation> allocations) {
        this.allocations = allocations;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
