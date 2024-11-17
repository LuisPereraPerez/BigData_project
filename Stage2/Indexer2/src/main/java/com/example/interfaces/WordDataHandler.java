package com.example.interfaces;

import java.util.List;

public interface WordDataHandler {
    String lemmAdd(String word);
    String cleanWord(String word);
    List<String> cleanAndSplit(String paragraph);
}
