package com.onlydust.marketplace.indexer.postgres.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
public class SearchProjectEntity {
    @Id
    private UUID id;
    private String name;
    private String shortDescription;
    private String longDescription;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Languages> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Category> categories;

    @Data
    public static class Languages {
        private String name;
    }

    @Data
    public static class Ecosystem {
        private String name;
    }

    @Data
    public static class Category {
        private String name;
    }


}