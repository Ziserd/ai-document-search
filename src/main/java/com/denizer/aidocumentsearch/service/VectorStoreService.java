
package com.denizer.aidocumentsearch.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    public List<Document> searchSimilarChunks(
            String query,
            String documentId,
            int topK
    ) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.5)
                .filterExpression(
                        "documentId == '" + documentId + "'"
                )
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}
