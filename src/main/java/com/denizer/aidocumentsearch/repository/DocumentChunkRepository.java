
package com.denizer.aidocumentsearch.repository;

import com.denizer.aidocumentsearch.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentDocumentId(String documentId);
}
