package com.denizer.aidocumentsearch.service;

import com.denizer.aidocumentsearch.entity.Document;
import com.denizer.aidocumentsearch.entity.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentChunkingService {

    private static final int CHUNK_SIZE = 20;
    private static final int CHUNK_OVERLAP = 3;

    public List<DocumentChunk> createChunks(
            Document document,
            List<String> lines
    ) {
        Objects.requireNonNull(document, "Document cannot be null");
        Objects.requireNonNull(lines, "Lines cannot be null");

        List<DocumentChunk> chunks = new ArrayList<>();

        int startIndex = 0;

        while (startIndex < lines.size()) {

            int endIndex = Math.min(
                    startIndex + CHUNK_SIZE,
                    lines.size()
            );

            String chunkContent = String.join(
                    "\n",
                    lines.subList(startIndex, endIndex)
            );

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(document);
            chunk.setContent(chunkContent);
            chunk.setStartLine(startIndex + 1);
            chunk.setEndLine(endIndex);

            chunks.add(chunk);

            if (endIndex == lines.size()) {
                break;
            }

            startIndex = endIndex - CHUNK_OVERLAP;
        }

        return chunks;
    }
}
