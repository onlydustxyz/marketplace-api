package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import com.onlydust.marketplace.indexer.postgres.entity.SearchContributorEntity;
import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Component
@Slf4j
public class SearchRepository {

    private final ElasticSearchHttpClient elasticSearchHttpClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Supplier<SearchResponse> EMPTY_RESPONSE =
            () -> new SearchResponse()
                    .hasMore(false)
                    .results(List.of())
                    .facets(List.of())
                    .totalItemNumber(0)
                    .nextPageIndex(0)
                    .totalPageNumber(0);

    public SearchResponse searchAll(final String keyword,
                                    final SearchItemType searchItemType,
                                    final Map<Facet, String> facets,
                                    final Integer from,
                                    final Integer size) {
        final String index = getIndexFromType(searchItemType);
        return elasticSearchHttpClient.send("/%s/_search".formatted(index), HttpMethod.POST,
                        buildSearchQuery(keyword, searchItemType, facets, from, size), JsonNode.class)
                .map(jsonNode -> searchResponseFromJsonNode(jsonNode, from, size))
                .orElseGet(EMPTY_RESPONSE);
    }

    public SuggestResponse suggest(String keyword, SearchItemType type) {
        final String index = getIndexFromType(type);
        return elasticSearchHttpClient.send("/%s/_search".formatted(index), HttpMethod.POST,
                        buildSuggestQuery(keyword, type), JsonNode.class)
                .map(this::suggestResponseFromJsonNode)
                .orElseGet(SuggestResponse::new);
    }

    private SuggestResponse suggestResponseFromJsonNode(final JsonNode jsonNode) {
        if (jsonNode.get("hits").get("total").get("value").asInt() == 0) {
            return new SuggestResponse();
        }
        final JsonNode firstDocument = jsonNode.get("hits").get("hits").get(0);
        final String index = firstDocument.get("_index").textValue();
        return switch (index) {
            case "od-projects" -> new SuggestResponse().value(firstDocument.get("_source").get("name").textValue());
            case "od-contributors" -> new SuggestResponse().value(firstDocument.get("_source").get("githubLogin").textValue());
            case null, default -> new SuggestResponse();
        };
    }

    public static ElasticSearchQuery<JsonNode> buildSuggestQuery(final String keyword, final SearchItemType type) {
        final ObjectNode query = objectMapper.createObjectNode();
        if (isNull(type)) {
            final ObjectNode bool = objectMapper.createObjectNode();
            final ArrayNode should = bool.putArray("should");

            final ObjectNode projectPrefix = objectMapper.createObjectNode();
            final ObjectNode projectNamePrefix = objectMapper.createObjectNode();
            projectNamePrefix.put("name", keyword);
            projectPrefix.put("prefix", projectNamePrefix);

            final ObjectNode contributorGithubLoginPrefix = objectMapper.createObjectNode();
            final ObjectNode contributorPrefix = objectMapper.createObjectNode();
            contributorGithubLoginPrefix.put("githubLogin", keyword);
            contributorPrefix.put("prefix", contributorGithubLoginPrefix);

            should.add(projectPrefix);
            should.add(contributorPrefix);
            query.put("bool", bool);
        } else if (type == SearchItemType.PROJECT) {
            final ObjectNode prefix = objectMapper.createObjectNode();
            prefix.put("name", keyword);
            query.put("prefix", prefix);
        } else if (type == SearchItemType.CONTRIBUTOR) {
            final ObjectNode prefix = objectMapper.createObjectNode();
            prefix.put("githubLogin", keyword);
            query.put("prefix", prefix);
        }
        return ElasticSearchQuery.<JsonNode>builder()
                .query(query)
                .from(0)
                .size(1)
                .build();
    }


    private static String getIndexFromType(SearchItemType searchItemType) {
        return isNull(searchItemType) ? "od-*" : switch (searchItemType) {
            case PROJECT -> ElasticSearchAdapter.PROJECTS_INDEX;
            case CONTRIBUTOR -> ElasticSearchAdapter.CONTRIBUTORS_INDEX;
        };
    }

    private static ElasticSearchQuery<QueryString> buildSearchQuery(final String keyword,
                                                                    final SearchItemType searchItemType,
                                                                    final Map<Facet, String> facets,
                                                                    final Integer from,
                                                                    final Integer size) {
        ElasticSearchQuery<QueryString> query = ElasticSearchQuery.<QueryString>builder()
                .from(from)
                .size(size)
                .query(QueryString.fromKeyword(keyword))
                .build();
        if (searchItemType == SearchItemType.PROJECT) {
            query = query.withFacets();
        }
        return query;
    }

    private SearchResponse searchResponseFromJsonNode(final JsonNode jsonNode, final Integer from, final Integer size) {
        try {
            final SearchResponse searchResponse = new SearchResponse();
            final int total = jsonNode.get("hits").get("total").get("value").asInt();
            if (total == 0) {
                return EMPTY_RESPONSE.get();
            }
            mapPagination(from, size, total, searchResponse);
            final JsonNode documents = jsonNode.get("hits").get("hits");
            for (JsonNode document : documents) {
                final String index = document.get("_index").textValue();
                if (index.equals(ElasticSearchAdapter.PROJECTS_INDEX)) {
                    mapProject(document, searchResponse);

                } else if (index.equals(ElasticSearchAdapter.CONTRIBUTORS_INDEX)) {
                    mapContributors(document, searchResponse);

                } else {
                    LOGGER.warn("Unexpected index %s".formatted(index));
                }
            }
            if (jsonNode.has("aggregations")) {
                searchResponse.facets(mapAggregationsToFacets(jsonNode.get("aggregations")));
            }
            return searchResponse;
        } catch (Exception e) {
            throw OnlyDustException.internalServerError(e.getMessage());
        }
    }

    private List<SearchFacetResponse> mapAggregationsToFacets(JsonNode aggregations) {
        final List<SearchFacetResponse> facets = new ArrayList<>();
        for (Facet facet : Facet.values()) {
            if (aggregations.has(facet.aggregationName)) {
                aggregations.get(facet.aggregationName).get("buckets").iterator().forEachRemaining(bucket -> {
                    facets.add(new SearchFacetResponse()
                            .name(bucket.get("key").textValue())
                            .type(facet.searchFacetType)
                            .count(bucket.get("doc_count").asInt())
                    );
                });
            }
        }
        return facets.stream().sorted(Comparator.comparing(SearchFacetResponse::getCount).reversed()).toList();
    }

    private static void mapProject(JsonNode document, SearchResponse searchResponse) {
        try {
            final SearchProjectEntity searchProjectEntity = objectMapper.treeToValue(document.get("_source"), SearchProjectEntity.class);
            searchResponse.addResultsItem(new SearchItemResponse()
                    .type(SearchItemType.PROJECT)
                    .project(
                            new ProjectAdvancedSearchResponse()
                                    .id(searchProjectEntity.getId())
                                    .name(searchProjectEntity.getName())
                                    .slug(searchProjectEntity.getSlug())
                                    .shortDescription(searchProjectEntity.getShortDescription())
                                    .forkCount(searchProjectEntity.getForkCount())
                                    .starCount(searchProjectEntity.getStarCount())
                                    .contributorCount(searchProjectEntity.getContributorCount())
                                    .categories(isNull(searchProjectEntity.getCategories()) ? null :
                                            searchProjectEntity.getCategories().stream().map(SearchProjectEntity.Category::getName).toList())
                                    .ecosystems(isNull(searchProjectEntity.getEcosystems()) ? null :
                                            searchProjectEntity.getEcosystems().stream().map(SearchProjectEntity.Ecosystem::getName).toList())
                                    .languages(isNull(searchProjectEntity.getLanguages()) ? null :
                                            searchProjectEntity.getLanguages().stream().map(SearchProjectEntity.Languages::getName).toList())

                    ));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to deserialize project {} from ElasticSearch", document);
        }
    }

    private static void mapContributors(JsonNode document, SearchResponse searchResponse) {
        try {
            final SearchContributorEntity searchContributorEntity = objectMapper.treeToValue(document.get("_source"), SearchContributorEntity.class);
            searchResponse.addResultsItem(new SearchItemResponse()
                    .type(SearchItemType.CONTRIBUTOR)
                    .contributor(new ContributorAdvancedSearchResponse()
                            .githubId(searchContributorEntity.getGithubId())
                            .githubLogin(searchContributorEntity.getGithubLogin())
                            .bio(searchContributorEntity.getBio())
                            .issueCount(searchContributorEntity.getIssueCount())
                            .projectCount(searchContributorEntity.getProjectCount())
                            .contributionCount(searchContributorEntity.getContributionCount())
                            .pullRequestCount(searchContributorEntity.getPullRequestCount())
                            .htmlUrl(searchContributorEntity.getHtmlUrl())
                    )
            );
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to deserialize contributors {} from ElasticSearch", document);
        }
    }

    private static void mapPagination(Integer from, Integer size, int total, SearchResponse searchResponse) {
        final int totalNumberOfPage = PaginationHelper.calculateTotalNumberOfPage(size, total);
        searchResponse.setTotalPageNumber(totalNumberOfPage);
        searchResponse.setTotalItemNumber(total);
        searchResponse.setNextPageIndex(PaginationHelper.nextPageIndex(from / size, totalNumberOfPage));
        searchResponse.setHasMore(total - from > size);
    }

    @Builder(toBuilder = true)
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class ElasticSearchQuery<Query> {
        Integer from;
        Integer size;
        Query query;
        JsonNode aggs;

        public ElasticSearchQuery<Query> withFacets() {
            final ObjectNode aggregations = objectMapper.createObjectNode();
            for (Facet value : Facet.values()) {
                aggregations.putObject(value.aggregationName)
                        .putObject("terms")
                        .put("field", value.fieldName);
            }
            return this.toBuilder().aggs(aggregations).build();
        }
    }

    @Builder
    @Data
    public static final class QueryString {
        @JsonProperty("query_string")
        Query queryString;

        public static QueryString fromKeyword(String keyword) {
            return QueryString.builder()
                    .queryString(Query.builder()
                            .query("*%s*".formatted(keyword))
                            .build())
                    .build();
        }
    }

    @Builder
    @Data
    public static final class Query {
        String query;
    }


    @AllArgsConstructor
    public enum Facet {
        ECOSYSTEMS("ecosystems_facet", "ecosystems.name.keyword", SearchFacetType.ECOSYSTEM),
        LANGUAGES("languages_facet", "languages.name.keyword", SearchFacetType.LANGUAGE),
        CATEGORIES("categories_facet", "categories.name.keyword", SearchFacetType.CATEGORY);

        private String aggregationName;
        private String fieldName;
        private SearchFacetType searchFacetType;
    }
}
