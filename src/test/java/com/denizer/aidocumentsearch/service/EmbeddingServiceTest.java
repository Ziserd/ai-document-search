package com.denizer.aidocumentsearch.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmbeddingServiceTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void shouldGenerateEmbeddingForText() {
        String text = "Database connection failed";

        float[] embedding = embeddingService.generateEmbedding(text);

        assertNotNull(embedding);
        assertEquals(768, embedding.length);
    }
}