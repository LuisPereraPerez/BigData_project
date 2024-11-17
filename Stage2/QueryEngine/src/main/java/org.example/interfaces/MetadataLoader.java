package org.example.interfaces;

import org.example.model.Metadata;

import java.util.Map;

public interface MetadataLoader {
    Map<Integer, Metadata> loadMetadata(String filePath) throws Exception;
}
