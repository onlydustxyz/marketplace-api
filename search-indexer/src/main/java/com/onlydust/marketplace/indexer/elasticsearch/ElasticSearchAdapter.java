package com.onlydust.marketplace.indexer.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public static String PROJECTS_INDEX = "projects";
    public static String CONTRIBUTORS_INDEX = "contributors";

    public void bulkIndexation(List<SearchProjectEntity> projects) {
        final var bulkRequestBody = new StringBuilder();

        for (SearchProjectEntity project : projects) {
            final var action = objectMapper.createObjectNode();
            final var index = objectMapper.createObjectNode();
            index.put("_index", PROJECTS_INDEX);
            index.put("_id", project.getId().toString());
            action.set("index", index);
            try {
                bulkRequestBody.append(objectMapper.writeValueAsString(action)).append("\n");
                bulkRequestBody.append(objectMapper.writeValueAsString(project)).append("\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        elasticSearchHttpClient.sendHttpRequest(HttpRequest.newBuilder()
                .uri(URI.create(elasticSearchHttpClient.elasticSearchProperties.getBaseUri() + "/_bulk?pretty"))
                .header("Content-Type", "application/x-ndjson")
                .POST(HttpRequest.BodyPublishers.ofString(bulkRequestBody.toString()))
                .build());

        LOGGER.info("Successfully indexed {} documents", projects.size());
    }
}
