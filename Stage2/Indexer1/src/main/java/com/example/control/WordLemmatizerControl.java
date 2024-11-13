package com.example.control;

import com.example.interfaces.WordLemmatizer;

public class WordLemmatizerControl implements WordLemmatizer {
    @Override
    public String lemmatize(String word){
        return word.toLowerCase();
    }
}