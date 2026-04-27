package com.diceduel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ability_packs")
public class AbilityPackEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    private String fileName;

    private String contentType;

    private LocalDateTime createdAt;

    public AbilityPackEntity() {
    }

    public AbilityPackEntity(String id, String name, String description, String fileName, String contentType, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fileName = fileName;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
