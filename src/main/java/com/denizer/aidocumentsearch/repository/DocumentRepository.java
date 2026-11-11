package com.denizer.aidocumentsearch.repository;

import com.denizer.aidocumentsearch.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentId(String documentId);

    boolean existsByDocumentId(String documentId);
}