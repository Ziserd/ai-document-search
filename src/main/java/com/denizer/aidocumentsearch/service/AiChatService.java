
package com.denizer.aidocumentsearch.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateAnswer(String question, String context) {

        return chatClient.prompt()
                .system("""
                        You are an AI assistant specialized in analyzing
                        TXT and LOG files.

                        Answer the user's question using only the
                        provided context.

                        If the context does not contain enough
                        information, clearly say that you cannot
                        determine the answer from the provided file.

                        Do not invent log entries or technical details.
                        """)
                .user("""
                        Question:
                        %s

                        Context:
                        %s
                        """.formatted(question, context))
                .call()
                .content();
    }
}
