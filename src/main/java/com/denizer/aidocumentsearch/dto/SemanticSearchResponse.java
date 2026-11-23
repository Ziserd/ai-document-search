
package com.denizer.aidocumentsearch.dto;

import java.util.List;

public record SemanticSearchResponse(
        String documentId,
        String query,
        int resultCount,
        List<SemanticSearchResult> results
) {
}
