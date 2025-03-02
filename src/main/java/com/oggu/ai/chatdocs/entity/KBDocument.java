package com.oggu.ai.chatdocs.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Author : bhask
 * Created : 02-22-2025
 */
@Entity
@Table(name = "kb_documents")
public class KBDocument implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kb_documents_seq")
    @SequenceGenerator(name = "pdf_documents_seq", sequenceName = "kb_documents_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Lob
    @Column(name = "data", nullable = false)
    private String data;  // CLOB data will be stored as String

    @Column(name = "file_chunks_number")
    private Integer fileChunksNumber;

    // Default constructor
    public KBDocument() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getFileChunksNumber() {
        return fileChunksNumber;
    }

    public void setFileChunksNumber(Integer fileChunksNumber) {
        this.fileChunksNumber = fileChunksNumber;
    }

    // Optional: Override toString, equals, hashcode
}
