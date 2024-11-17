package com.example.control;

import com.example.interfaces.WordCleaner;

import java.text.Normalizer;

public class WordCleanerControl implements WordCleaner {

    public WordCleanerControl() {
    }

    @Override
    public String cleanWord(String word) {
        word = word.replaceAll("['’].*$", "");
        word = word.replaceAll("[^\\p{L}]", "");

        word = Normalizer.normalize(word, Normalizer.Form.NFD);
        word = word.replaceAll("[^\\p{ASCII}]", "");

        return word.trim();
    }
}
