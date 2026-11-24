
package com.denizer.aidocumentsearch.dto;

import java.util.List;

public record RagResponse(
        String documentId,
        String question,
        String answer,
        List<RagSource> sources
) {
}
