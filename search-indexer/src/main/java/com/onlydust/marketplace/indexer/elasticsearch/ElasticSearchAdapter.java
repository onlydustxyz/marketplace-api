package com.onlydust.marketplace.indexer.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlydust.marketplace.indexer.elasticsearch.properties.ElasticSearchProperties;
import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
@Data
public class ElasticSearchAdapter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private ElasticSearchProperties elasticSearchProperties;
    final HttpClient httpClient = HttpClient.newHttpClient();

    @SneakyThrows
    public void bulkIndex(List<SearchProjectEntity> projects) {
        final var bulkRequestBody = new StringBuilder();
        ;

        for (SearchProjectEntity project : projects) {
            final var action = objectMapper.createObjectNode();
            final var index = objectMapper.createObjectNode();
            index.put("_index", "pierre-projects");
            index.put("_id", project.getId().toString());
            action.set("index", index);
            bulkRequestBody.append(objectMapper.writeValueAsString(action)).append("\n");
            bulkRequestBody.append(objectMapper.writeValueAsString(project)).append("\n");
        }

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(elasticSearchProperties.getUrl() + "/_bulk?pretty"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bulkRequestBody.toString()))
                .header("Authorization", "ApiKey %s".formatted(elasticSearchProperties.getApiKey()))
                .build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            LOGGER.error("Failed to bulk index documents: {}", response.body());
            throw new RuntimeException("Failed to bulk index documents: " + response.statusCode());
        }

        LOGGER.info("Successfully indexed {} documents", projects.size());
    }
}
