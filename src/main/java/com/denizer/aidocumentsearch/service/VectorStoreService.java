
package com.denizer.aidocumentsearch.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void saveChunk(
            String content,
            String documentId,
            int startLine,
            int endLine
    ) {
        Document document = new Document(
                content,
                Map.of(
                        "documentId", documentId,
                        "startLine", startLine,
                        "endLine", endLine
                )
        );

        vectorStore.add(List.of(document));
    }
}
