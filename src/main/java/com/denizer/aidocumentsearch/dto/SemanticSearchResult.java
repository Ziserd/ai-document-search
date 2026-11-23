
package com.denizer.aidocumentsearch.dto;

public record SemanticSearchResult(
        String content,
        int startLine,
        int endLine,
        double score
) {
}
