package onlydust.com.marketplace.api.read.repositories;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import onlydust.com.marketplace.api.contract.model.ProjectAdvancedSearchResponse;
import onlydust.com.marketplace.api.contract.model.SearchItemResponse;
import onlydust.com.marketplace.api.contract.model.SearchItemType;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class SearchRepository {

    private final ElasticSearchHttpClient elasticSearchHttpClient;

    public SearchResponse searchAll(final String keyword, final Integer from, final Integer size) {
        return elasticSearchHttpClient.send("/_all/search", HttpMethod.POST,
                        ElasticSearchQuery.<SimpleQueryString>builder()
                                .from(from)
                                .size(size)
                                .query(SimpleQueryString.builder()
                                        .simpleQueryString(Query.builder()
                                                .query(keyword)
                                                .build())
                                        .build()), JsonNode.class)
                .map(this::searchResponseFromJsonNode)
                .orElseGet(() -> new SearchResponse().hasMore(false).results(List.of()).totalItemNumber(0).totalPageNumber(0));
    }

    private SearchResponse searchResponseFromJsonNode(final JsonNode jsonNode) {
        final SearchResponse searchResponse = new SearchResponse();
        final JsonNode documents = jsonNode.get("hits").get("hits");
        for (JsonNode document : documents) {
            if (document.get("_index").textValue().equals("projects")) {
                searchResponse.addResultsItem(new SearchItemResponse()
                        .type(SearchItemType.PROJECT)
                        .project(
                                new ProjectAdvancedSearchResponse()
                                        .id(UUID.fromString(document.get("_source").get("id").textValue()))
                                        .name(document.get("_source").get("name").textValue())
                                        .shortDescription(document.get("_source").get("short_description").textValue())

                        ));
            }
        }
        return searchResponse;
    }

    @Builder
    private static final class ElasticSearchQuery<Query> {
        Integer from;
        Integer size;
        Query query;
    }

    @Builder
    private static final class SimpleQueryString {
        @JsonProperty("simple_query_string")
        Query simpleQueryString;
    }

    @Builder
    private static final class Query {
        String query;
    }
}
