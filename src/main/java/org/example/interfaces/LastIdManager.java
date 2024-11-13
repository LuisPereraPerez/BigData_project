package org.example.interfaces;

public interface LastIdManager {
    int getLastDownloadedId();
    void updateLastDownloadedId(int lastId);
}
