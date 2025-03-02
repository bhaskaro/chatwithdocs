package com.oggu.ai.chatdocs.repository;

import com.oggu.ai.chatdocs.entity.KBDocument;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author : bhask
 * Created : 02-22-2025
 */
@Repository
public interface KBDocumentRepository extends CrudRepository<KBDocument, Long> {
    // The CrudRepository already provides basic CRUD operations.
    // You can define custom query methods here if needed.

    public List<KBDocument> findByFileName(String fileName);
}

