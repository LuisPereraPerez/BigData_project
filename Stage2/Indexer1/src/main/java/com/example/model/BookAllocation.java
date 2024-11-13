package com.example.model;

import java.util.List;

public class BookAllocation {
    private int times;
    private List<Position> positions;

    public BookAllocation(int times, List<Position> positions) {
        this.times = times;
        this.positions = positions;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
}
