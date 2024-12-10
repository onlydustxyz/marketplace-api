package com.onlydust.marketplace.indexer.postgres.entity;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchDocument;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class SearchProjectEntity implements ElasticSearchDocument<UUID> {
    @Id
    private UUID id;
    private String slug;
    private String name;
    private String shortDescription;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Languages> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<Category> categories;
    private Integer contributorCount;
    private Integer starCount;
    private Integer forkCount;

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

    @Override
    public UUID getDocumentId() {
        return id;
    }
}