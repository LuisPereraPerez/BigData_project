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

    // Inicializar el pipeline solo una vez al cargar la clase
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

        // Obtener el lema de la palabra
        for (CoreLabel token : document.tokens()) {
            return token.get(CoreAnnotations.LemmaAnnotation.class);
        }
        return word; // Devuelve la palabra original si no se encuentra el lema
    }

    @Override
    public String cleanWord(String word) {
        // Eliminar sufijos como "'s" o "’s" en palabras
        word = word.replaceAll("(['’]s)$", ""); // Elimina el sufijo 's o ’s al final de una palabra
        // Eliminar caracteres especiales como ' o ’ en cualquier parte de la palabra
        word = word.replaceAll("['’]", ""); // Elimina cualquier apóstrofe en el resto de la palabra
        // Verificar si la palabra empieza con "_" o contiene algún número
        if (word.startsWith("_") || word.matches(".*\\d.*")) {
            return ""; // Si la palabra contiene "_" al inicio o cualquier número, se devuelve vacía
        }
        // Eliminar caracteres especiales dejando solo letras
        word = word.replaceAll("[^\\p{L}]", ""); // Mantiene solo letras
        // Normalizar y eliminar caracteres no ASCII
        word = Normalizer.normalize(word, Normalizer.Form.NFD);
        word = word.replaceAll("[^\\p{ASCII}]", "");
        return word.trim().toLowerCase();
    }


    @Override
    public List<String> cleanAndSplit(String paragraph) {
        // Eliminar sufijos como "'s" o "’s" en cada palabra del párrafo
        String[] words = paragraph.replaceAll("(['’]s\\b)", "") // Elimina sufijos de posesión como "’s"
                .replaceAll("['’]", "") // Elimina cualquier apóstrofe restante
                .replaceAll("[^\\p{L}\\s]", "") // Elimina caracteres especiales excepto letras y espacios
                .toLowerCase()
                .split("\\s+"); // Divide en palabras por espacios
        return new ArrayList<>(Arrays.asList(words));
    }
}
