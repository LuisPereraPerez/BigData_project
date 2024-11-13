package com.example.control;

import com.example.control.*;
import com.example.interfaces.*;

public class Main {
    private static final int NUM_BOOKS = 10;
    private static final String SAVE_DIR = "datalake/books";
    private static final int MAX_DOWNLOAD_ATTEMPTS = 3;

    public static void main(String[] args) {
        BookDownloader downloader = new GutenbergBookDownloader(SAVE_DIR);
        MetadataExtractor metadataExtractor = new GutenbergMetadataExtractor();
        BookProcessor bookProcessor = new GutenbergBookProcessor();
        MetadataWriter metadataWriter = new CSVMetadataWriter();
        LastIdManager lastIdManager = new FileLastIdManager();

        BookManager bookManager = new BookManager(downloader, metadataExtractor, bookProcessor, metadataWriter, SAVE_DIR);
        GutenbergCrawler crawler = new GutenbergCrawlerSequential(bookManager, lastIdManager, MAX_DOWNLOAD_ATTEMPTS);

        crawler.startCrawling(NUM_BOOKS);
    }
}
