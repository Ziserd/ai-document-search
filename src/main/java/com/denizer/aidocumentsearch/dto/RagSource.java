
package com.denizer.aidocumentsearch.dto;

public record RagSource(
        int startLine,
        int endLine,
        String content
) {
}
