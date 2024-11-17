package org.example.model;

public class Metadata {
    private final int id;
    private final String title;
    private final String author;

    public Metadata(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
