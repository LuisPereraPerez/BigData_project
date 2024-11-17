package com.example.benchmark;

import org.example.control.CSVMetadataLoader;
import org.example.control.JSONIndexLoader;
import org.example.control.TSVIndexLoader;
import org.example.control.SimpleQueryProcessor;
import org.example.interfaces.IndexLoader;
import org.example.model.Metadata;
import org.example.model.WordData;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class IndexSearchBenchmark {

    private Map<Integer, Metadata> metadata;
    private Map<String, Map<Integer, WordData>> jsonIndex;
    private Map<String, Map<Integer, WordData>> tsvIndex;
    private SimpleQueryProcessor jsonProcessor;
    private SimpleQueryProcessor tsvProcessor;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        String metadataPath = "datalake/metadata.csv";


        CSVMetadataLoader metadataLoader = new CSVMetadataLoader();
        metadata = metadataLoader.loadMetadata(metadataPath);


        IndexLoader jsonLoader = new JSONIndexLoader();
        jsonIndex = jsonLoader.loadIndex("datamart/reverse_indexes_indexer1");
        jsonProcessor = new SimpleQueryProcessor(jsonIndex, metadata);


        IndexLoader tsvLoader = new TSVIndexLoader();
        tsvIndex = tsvLoader.loadIndex("datamart/reverse_indexes_indexer2");
        tsvProcessor = new SimpleQueryProcessor(tsvIndex, metadata);
    }

    @Benchmark
    public void searchJSON5Words() {
        searchWords(jsonProcessor, 5);
    }

    @Benchmark
    public void searchJSON10Words() {
        searchWords(jsonProcessor, 10);
    }

    @Benchmark
    public void searchJSON25Words() {
        searchWords(jsonProcessor, 25);
    }

    @Benchmark
    public void searchJSON50Words() {
        searchWords(jsonProcessor, 50);
    }

    @Benchmark
    public void searchTSV5Words() {
        searchWords(tsvProcessor, 5);
    }

    @Benchmark
    public void searchTSV10Words() {
        searchWords(tsvProcessor, 10);
    }

    @Benchmark
    public void searchTSV25Words() {
        searchWords(tsvProcessor, 25);
    }

    @Benchmark
    public void searchTSV50Words() {
        searchWords(tsvProcessor, 50);
    }

    private void searchWords(SimpleQueryProcessor processor, int wordCount) {
        String[] words = {
                "independence", "banana", "cherry", "date", "elephant",
                "forest", "grape", "house", "island", "jungle",
                "kite", "lemon", "mountain", "night", "ocean",
                "pineapple", "queen", "river", "sun", "tree",
                "umbrella", "violet", "water", "xylophone", "yellow",
                "zebra", "cloud", "dream", "fire", "garden",
                "heart", "idea", "joy", "king", "lamp",
                "moon", "novel", "orange", "pencil", "quiet",
                "rose", "stone", "travel", "universe", "victory",
                "whale", "xenon", "youth", "zoo", "world"
        };

        for (int i = 0; i < wordCount; i++) {
            processor.processQuery(words[i]);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IndexSearchBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(2)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }
}
