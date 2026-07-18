package com.example.rbac.repository;

import com.example.rbac.model.SupportingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportingDocumentRepository extends JpaRepository<SupportingDocument, Long> {
    List<SupportingDocument> findByUserId(Long userId);
}