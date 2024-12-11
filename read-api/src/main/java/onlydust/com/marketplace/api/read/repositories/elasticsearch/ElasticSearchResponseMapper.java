package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.postgres.entity.SearchContributorEntity;
import com.onlydust.marketplace.indexer.postgres.entity.SearchProjectEntity;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter.CONTRIBUTORS_INDEX;
import static com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter.PROJECTS_INDEX;
import static java.util.Objects.isNull;


public interface ElasticSearchResponseMapper {
    ObjectMapper objectMapper = new ObjectMapper();
    Supplier<SearchResponse> EMPTY_RESPONSE =
            () -> new SearchResponse()
                    .hasMore(false)
                    .results(List.of())
                    .totalItemNumber(0)
                    .nextPageIndex(0)
                    .totalPageNumber(0);

    static SuggestResponse jsonNodeToSuggestResponse(final JsonNode jsonNode) {
        if (jsonNode.get("hits").get("total").get("value").asInt() == 0) {
            return new SuggestResponse();
        }
        final JsonNode firstDocument = jsonNode.get("hits").get("hits").get(0);
        final String index = firstDocument.get("_index").textValue();
        if (index.equals(PROJECTS_INDEX)) {
            return new SuggestResponse().value(firstDocument.get("_source").get("name").textValue());
        } else if (index.equals(CONTRIBUTORS_INDEX)) {
            return new SuggestResponse().value(firstDocument.get("_source").get("githubLogin").textValue());
        }
        return new SuggestResponse();
    }

    static SearchResponse jsonNodeToSearchResponse(final JsonNode jsonNode, final int from, final int size) {
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
                    throw OnlyDustException.internalServerError("Unknown index %s".formatted(index));
                }
            }
            if (jsonNode.has("aggregations")) {
                final JsonNode aggregations = jsonNode.get("aggregations");
                searchResponse.projectFacets(mapAggregationsToProjectFacets(aggregations));
                searchResponse.setTypeFacets(mapAggregationsToTypeFacets(aggregations));
            }
            return searchResponse;
        } catch (Exception e) {
            throw OnlyDustException.internalServerError(e.getMessage());
        }
    }

    private static ProjectFacetsResponse mapAggregationsToProjectFacets(JsonNode aggregations) {
        final ProjectFacetsResponse projectFacetsResponse = new ProjectFacetsResponse();
        for (ProjectFacet projectFacet : ProjectFacet.values()) {
            final List<SearchFacetResponse> facets = new ArrayList<>();
            if (aggregations.has(projectFacet.getAggregationName())) {
                aggregations.get(projectFacet.getAggregationName()).get("buckets").iterator().forEachRemaining(bucket -> {
                    facets.add(new SearchFacetResponse()
                            .name(bucket.get("key").textValue())
                            .count(bucket.get("doc_count").asInt())
                    );
                });
                final List<SearchFacetResponse> sortedFacets = facets.stream().sorted(Comparator.comparing(SearchFacetResponse::getCount).reversed()).toList();
                if (projectFacet == ProjectFacet.CATEGORIES) {
                    projectFacetsResponse.categories(sortedFacets);
                }
                if (projectFacet == ProjectFacet.LANGUAGES) {
                    projectFacetsResponse.languages(sortedFacets);
                }
                if (projectFacet == ProjectFacet.ECOSYSTEMS) {
                    projectFacetsResponse.ecosystems(sortedFacets);
                }
            }
        }
        return projectFacetsResponse;
    }

    private static TypesFacetResponse mapAggregationsToTypeFacets(JsonNode aggregations) {
        final TypesFacetResponse typesFacetResponse = new TypesFacetResponse();
        if (aggregations.has(TypeFacet.INDEXES.getAggregationName())) {
            final List<SearchFacetResponse> facets = new ArrayList<>();
            aggregations.get(TypeFacet.INDEXES.getAggregationName()).get("buckets").iterator().forEachRemaining(bucket -> {
                final String indexName = bucket.get("key").textValue();
                if (indexName.equals(ElasticSearchAdapter.PROJECTS_INDEX)) {
                    facets.add(new SearchFacetResponse()
                            .name("Projects")
                            .count(bucket.get("doc_count").asInt()));
                } else if (indexName.equals(ElasticSearchAdapter.CONTRIBUTORS_INDEX)) {
                    facets.add(new SearchFacetResponse()
                            .name("Contributors")
                            .count(bucket.get("doc_count").asInt()));
                } else {
                    throw OnlyDustException.internalServerError("Unknown index %s".formatted(indexName));
                }
            });
            typesFacetResponse.types(facets.stream().sorted(Comparator.comparing(SearchFacetResponse::getCount).reversed()).toList());
        }
        return typesFacetResponse;
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
            throw OnlyDustException.internalServerError("Failed to deserialize projects %s from ElasticSearch".formatted(document));
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
            throw OnlyDustException.internalServerError("Failed to deserialize contributors %s from ElasticSearch".formatted(document));
        }
    }

    static void mapPagination(final Integer from, final Integer size, int total, SearchResponse searchResponse) {
        final int totalNumberOfPage = PaginationHelper.calculateTotalNumberOfPage(size, total);
        searchResponse.setTotalPageNumber(totalNumberOfPage);
        searchResponse.setTotalItemNumber(total);
        searchResponse.setNextPageIndex(PaginationHelper.nextPageIndex(from / size, totalNumberOfPage));
        searchResponse.setHasMore(total - from > size);
    }


}
