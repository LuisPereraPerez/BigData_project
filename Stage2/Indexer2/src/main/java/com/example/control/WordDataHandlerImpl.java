package com.example.control;

import com.example.interfaces.WordDataHandler;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.text.Normalizer;
import java.util.*;

public class WordDataHandlerImpl implements WordDataHandler {

    private static final StanfordCoreNLP pipeline;

    static {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }
    @Override
    public String lemmAdd(String word) {
        word = word.toLowerCase();
        CoreDocument document = new CoreDocument(word);
        pipeline.annotate(document);

        for (CoreLabel token : document.tokens()) {
            return token.get(CoreAnnotations.LemmaAnnotation.class);
        }
        return word;
    }

    @Override
    public String cleanWord(String word) {
        word = word.replaceAll("(['’]s)$", "");
        word = word.replaceAll("['’]", "");
        if (word.startsWith("_") || word.matches(".*\\d.*")) {
            return "";
        }

        word = word.replaceAll("[^\\p{L}]", "");
        word = Normalizer.normalize(word, Normalizer.Form.NFD);
        word = word.replaceAll("[^\\p{ASCII}]", "");
        return word.trim().toLowerCase();
    }


    @Override
    public List<String> cleanAndSplit(String paragraph) {
        String[] words = paragraph.replaceAll("(['’]s\\b)", "")
                .replaceAll("['’]", "")
                .replaceAll("[^\\p{L}\\s]", "")
                .toLowerCase()
                .split("\\s+");
        return new ArrayList<>(Arrays.asList(words));
    }
}
