package com.denizer.aidocumentsearch.dto;

import jakarta.validation.constraints.NotBlank;

public record AskQuestionRequest(
        @NotBlank(message = "Question cannot be empty")
        String question
) {
}