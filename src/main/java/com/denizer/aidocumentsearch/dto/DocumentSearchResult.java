package com.denizer.aidocumentsearch.dto;

public record DocumentSearchResult(
        int lineNumber,
        String content
) {
}