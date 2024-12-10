package com.onlydust.marketplace.indexer.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlydust.marketplace.indexer.postgres.entity.SearchContributorEntity;
import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
@Data
public class ElasticSearchAdapter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ElasticSearchHttpClient elasticSearchHttpClient;
    public static String PROJECTS_INDEX = "od-projects";
    public static String CONTRIBUTORS_INDEX = "od-contributors";

    public void indexAllProjects(final List<SearchProjectEntity> projects) {
        indexAllDocuments(projects, PROJECTS_INDEX);
    }

    public void indexAllContributors(final List<SearchContributorEntity> contributors) {
        indexAllDocuments(contributors, CONTRIBUTORS_INDEX);
    }

    private <Id, Document extends ElasticSearchDocument<Id>> void indexAllDocuments(final List<Document> documents, final String indexName) {
        final var bulkRequestBody = new StringBuilder();

        for (ElasticSearchDocument<Id> document : documents) {
            final var action = objectMapper.createObjectNode();
            final var index = objectMapper.createObjectNode();
            index.put("_index", indexName);
            index.put("_id", document.getDocumentId().toString());
            action.set("index", index);
            try {
                bulkRequestBody.append(objectMapper.writeValueAsString(action)).append("\n");
                bulkRequestBody.append(objectMapper.writeValueAsString(document)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        elasticSearchHttpClient.sendHttpRequest(HttpRequest.newBuilder()
                .uri(URI.create(elasticSearchHttpClient.elasticSearchProperties.getBaseUri() + "/_bulk?pretty"))
                .header("Content-Type", "application/x-ndjson")
                .header("Authorization", "ApiKey %s".formatted(elasticSearchHttpClient.elasticSearchProperties.getApiKey()))
                .POST(HttpRequest.BodyPublishers.ofString(bulkRequestBody.toString()))
                .build());
    }
}
