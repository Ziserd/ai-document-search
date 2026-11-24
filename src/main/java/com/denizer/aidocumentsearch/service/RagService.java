
package com.denizer.aidocumentsearch.service;

import com.denizer.aidocumentsearch.dto.RagResponse;
import com.denizer.aidocumentsearch.dto.RagSource;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStoreService vectorStoreService;
    private final AiChatService aiChatService;

    public RagService(
            VectorStoreService vectorStoreService,
            AiChatService aiChatService
    ) {
        this.vectorStoreService = vectorStoreService;
        this.aiChatService = aiChatService;
    }

    public RagResponse askQuestion(
            String documentId,
            String question
    ) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }

        List<Document> relevantChunks =
                vectorStoreService.searchSimilarChunks(
                        question,
                        documentId,
                        5
                );

        if (relevantChunks.isEmpty()) {
            return new RagResponse(
                    documentId,
                    question,
                    "I could not find relevant information in the document.",
                    List.of()
            );
        }

        List<RagSource> sources = relevantChunks.stream()
                .map(document -> new RagSource(
                        ((Number) document.getMetadata().get("startLine")).intValue(),
                        ((Number) document.getMetadata().get("endLine")).intValue(),
                        document.getText()
                ))
                .toList();

        String context = sources.stream()
                .map(source -> """
                        Lines %d-%d:
                        %s
                        """.formatted(
                        source.startLine(),
                        source.endLine(),
                        source.content()
                ))
                .collect(Collectors.joining("\n"));

        String answer = aiChatService.generateAnswer(
                question,
                context
        );

        return new RagResponse(
                documentId,
                question,
                answer,
                sources
        );
    }
}
