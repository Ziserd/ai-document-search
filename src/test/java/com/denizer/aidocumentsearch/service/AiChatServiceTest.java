package com.denizer.aidocumentsearch.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class AiChatServiceTest {

    @Autowired
    private AiChatService aiChatService;

    @Test
    void shouldGenerateAnswerUsingProvidedContext() {

        String question = "Why did the database connection fail?";

        String context = """
                ERROR: Connection refused: localhost:5432
                ERROR: Database connection failed
                """;

        String answer = aiChatService.generateAnswer(
                question,
                context
        );

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        System.out.println("AI Answer: " + answer);
    }
}