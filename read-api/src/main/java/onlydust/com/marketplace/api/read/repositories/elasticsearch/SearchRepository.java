package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.model.SearchItemType;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import onlydust.com.marketplace.api.contract.model.SuggestResponse;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.read.repositories.elasticsearch.ElasticSearchResponseMapper.EMPTY_RESPONSE;
import static onlydust.com.marketplace.api.read.repositories.elasticsearch.ElasticSearchResponseMapper.jsonNodeToSearchResponse;

@AllArgsConstructor
@Component
@Slf4j
public class SearchRepository {

    private final ElasticSearchHttpClient elasticSearchHttpClient;

    public SearchResponse searchAll(final String keyword,
                                    final SearchItemType searchItemType,
                                    Map<ProjectFacet, List<String>> facets,
                                    final Integer from,
                                    final Integer size) {
        final String index = getIndexFromType(searchItemType);
        return elasticSearchHttpClient.send("/%s/_search".formatted(index), HttpMethod.POST,
                        buildSearchQuery(keyword, searchItemType, facets, from, size), JsonNode.class)
                .map(jsonNode -> jsonNodeToSearchResponse(jsonNode, from, size))
                .orElseGet(EMPTY_RESPONSE);
    }

    public SuggestResponse suggest(String keyword, SearchItemType type, Map<ProjectFacet, List<String>> facets) {
        final String index = getIndexFromType(type);
        return elasticSearchHttpClient.send("/%s/_search".formatted(index), HttpMethod.POST,
                        buildSuggestQuery(keyword, type, facets), JsonNode.class)
                .map(ElasticSearchResponseMapper::jsonNodeToSuggestResponse)
                .orElseGet(SuggestResponse::new);
    }

    public static JsonNode buildSuggestQuery(final String keyword, final SearchItemType type, final Map<ProjectFacet, List<String>> facets) {
        if (isNull(type)) {
            return ElasticSearchQuery.builder()
                    .withPrefixes(
                            ElasticSearchQuery.Prefix.builder()
                                    .keyword(keyword)
                                    .field("name")
                                    .build(),
                            ElasticSearchQuery.Prefix.builder()
                                    .keyword(keyword)
                                    .field("githubLogin")
                                    .build()
                    )
                    .build();
        } else if (type == SearchItemType.PROJECT) {
            return ElasticSearchQuery.builder()
                    .withPrefixes(
                            ElasticSearchQuery.Prefix.builder()
                                    .keyword(keyword)
                                    .field("name")
                                    .build()
                    )
                    .withMultipleTerms(projectFacetsToMultiTerms(facets))
                    .build();
        } else if (type == SearchItemType.CONTRIBUTOR) {
            return ElasticSearchQuery.builder()
                    .withPrefixes(
                            ElasticSearchQuery.Prefix.builder()
                                    .keyword(keyword)
                                    .field("githubLogin")
                                    .build()
                    )
                    .build();
        } else {
            throw OnlyDustException.badRequest("Bad request for unknown searchItemType %s".formatted(type));
        }
    }

    private static String getIndexFromType(SearchItemType searchItemType) {
        return isNull(searchItemType) ? "od-*" : switch (searchItemType) {
            case PROJECT -> ElasticSearchAdapter.PROJECTS_INDEX;
            case CONTRIBUTOR -> ElasticSearchAdapter.CONTRIBUTORS_INDEX;
        };
    }

    private static JsonNode buildSearchQuery(final String keyword,
                                             final SearchItemType searchItemType,
                                             final Map<ProjectFacet, List<String>> facets,
                                             final Integer from,
                                             final Integer size) {
        if (searchItemType == SearchItemType.PROJECT) {
            return ElasticSearchQuery.builder()
                    .withPagination(from, size)
                    .withQueryString(keyword)
                    .withAggregations(ProjectFacet.toAggregations())
                    .withMultipleTerms(projectFacetsToMultiTerms(facets))
                    .build();
        }else if (isNull(searchItemType)) {
            return ElasticSearchQuery.builder()
                    .withPagination(from, size)
                    .withQueryString(keyword)
                    .withAggregations(List.of(TypeFacet.INDEXES.toAggregation()))
                    .build();
        } else {
            return ElasticSearchQuery.builder()
                    .withPagination(from, size)
                    .withQueryString(keyword)
                    .build();
        }
    }

    private static List<ElasticSearchQuery.MultipleTerm> projectFacetsToMultiTerms(final Map<ProjectFacet, List<String>> facets) {
        if (isNull(facets)) {
            return null;
        }
        final List<ElasticSearchQuery.MultipleTerm> multiTerms = new ArrayList<>();
        for (Map.Entry<ProjectFacet, List<String>> projectFacetListEntry : facets.entrySet()) {
            multiTerms.add(ElasticSearchQuery.MultipleTerm.builder()
                    .terms(projectFacetListEntry.getValue())
                    .field(projectFacetListEntry.getKey().getField())
                    .build());
        }
        return multiTerms;
    }
}
