package onlydust.com.marketplace.api.read.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import io.netty.handler.codec.http.HttpMethod;
import onlydust.com.marketplace.api.contract.model.SearchItemResponse;
import onlydust.com.marketplace.api.contract.model.SearchItemType;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchRepositoryTest {

    private static final Faker faker = new Faker();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private SearchRepository searchRepository;
    private ElasticSearchHttpClient elasticSearchHttpClient;

    @BeforeEach
    public void setUp() {
        elasticSearchHttpClient = mock(ElasticSearchHttpClient.class);
        searchRepository = new SearchRepository(elasticSearchHttpClient);

    }

    @Test
    void should_map_empty_response() throws JsonProcessingException {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final int from = faker.number().numberBetween(0, 1000);
        final int size = faker.number().numberBetween(0, 1000);

        // When
        when(elasticSearchHttpClient.send("/_all/_search", HttpMethod.POST,
                SearchRepository.ElasticSearchQuery.<SearchRepository.SimpleQueryString>builder().from(from).size(size).query(SearchRepository.SimpleQueryString.builder().simpleQueryString(SearchRepository.Query.builder().query(keyword).build()).build()).build(), JsonNode.class)).thenReturn(Optional.of(objectMapper.readTree("""
                {
                   "took": 18,
                   "timed_out": false,
                   "_shards": {
                     "total": 28,
                     "successful": 28,
                     "skipped": 0,
                     "failed": 0
                   },
                   "hits": {
                     "total": {
                       "value": 0,
                       "relation": "eq"
                     },
                     "max_score": null,
                     "hits": []
                   }
                }
                """)));
        final SearchResponse searchResponse = searchRepository.searchAll(keyword, null, null, from, size);

        // Then
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getTotalItemNumber());
        assertEquals(0, searchResponse.getTotalPageNumber());
        assertEquals(0, searchResponse.getNextPageIndex());
        assertEquals(false, searchResponse.getHasMore());
        assertEquals(List.of(), searchResponse.getResults());
        assertEquals(List.of(), searchResponse.getFacets());
    }

    @Test
    void should_map_simple_query() throws JsonProcessingException {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final int from = faker.number().numberBetween(0, 1000);
        final int size = faker.number().numberBetween(0, 1000);

        // When
        when(elasticSearchHttpClient.send("/_all/_search", HttpMethod.POST,
                SearchRepository.ElasticSearchQuery.<SearchRepository.SimpleQueryString>builder().from(from).size(size).query(SearchRepository.SimpleQueryString.builder().simpleQueryString(SearchRepository.Query.builder().query(keyword).build()).build()).build(), JsonNode.class)).thenReturn(Optional.of(objectMapper.readTree("""
                {
                    "took": 13,
                    "timed_out": false,
                    "_shards": {
                        "total": 1,
                        "successful": 1,
                        "skipped": 0,
                        "failed": 0
                    },
                    "hits": {
                        "total": {
                            "value": 1,
                            "relation": "eq"
                        },
                        "max_score": 3.916677,
                        "hits": [
                            {
                                "_index": "projects",
                                "_id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                "_score": 3.916677,
                                "_source": {
                                    "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                    "name": "DogGPT",
                                    "slug": "doggpt",
                                    "shortDescription": "Chat GPT for cat lovers",
                                    "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                                    "languages": null,
                                    "ecosystems": [
                                        {
                                            "name": "Ethereum"
                                        }
                                    ],
                                    "categories": null
                                }
                            }
                        ]
                    }
                }
                """)));
        final SearchResponse searchResponse = searchRepository.searchAll(keyword, null, null, from, size);

        // Then
        assertNotNull(searchResponse);
        final SearchItemResponse searchItemResponse = searchResponse.getResults().get(0);
        assertEquals(UUID.fromString("61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17"), searchItemResponse.getProject().getId());
        assertEquals("DogGPT", searchItemResponse.getProject().getName());
        assertEquals("doggpt", searchItemResponse.getProject().getSlug());
        assertEquals("Chat GPT for cat lovers", searchItemResponse.getProject().getShortDescription());
        assertNull(searchItemResponse.getProject().getLanguages());
        assertNull(searchItemResponse.getProject().getCategories());
        assertEquals(1, searchItemResponse.getProject().getEcosystems().size());
        assertEquals("Ethereum", searchItemResponse.getProject().getEcosystems().get(0));
    }

    @Test
    void should_map_pagination() throws JsonProcessingException {
        assertPagination(0, 10, 9, 1, 9, 0, false);
        assertPagination(0, 10, 12, 2, 12, 1, true);
        assertPagination(5, 5, 12, 3, 12, 2, true);
        assertPagination(5, 10, 5, 1, 5, 0, false);
    }

    @Test
    void should_map_project_facets_to_query() {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final int from = 0;
        final int size = 10;

        // When
        searchRepository.searchAll(keyword, SearchItemType.PROJECT, null, from, size);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString>> elasticSearchQueryArgumentCaptor =
                ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
        final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                classArgumentCaptor.capture());
        assertEquals("/projects/_search", stringArgumentCaptor.getValue());
        final SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString> query = elasticSearchQueryArgumentCaptor.getValue();
        assertEquals(keyword, query.query.simpleQueryString.query);
        assertEquals(from, query.from);
        assertEquals(size, query.size);
        assertEquals("""
                {
                  "ecosystems_facet" : {
                    "terms" : {
                      "field" : "ecosystems.name.enum"
                    }
                  },
                  "languages_facet" : {
                    "terms" : {
                      "field" : "languages.name.enum"
                    }
                  },
                  "categories_facet" : {
                    "terms" : {
                      "field" : "categories.name.enum"
                    }
                  }
                }""", query.aggs.toPrettyString());
    }

    @Test
    void should_not_map_facets_to_query_given_all() {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final int from = 0;
        final int size = 10;

        // When
        searchRepository.searchAll(keyword, null, null, from, size);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString>> elasticSearchQueryArgumentCaptor =
                ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
        final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                classArgumentCaptor.capture());
        assertEquals("/_all/_search", stringArgumentCaptor.getValue());
        final SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString> query = elasticSearchQueryArgumentCaptor.getValue();
        assertEquals(keyword, query.query.simpleQueryString.query);
        assertEquals(from, query.from);
        assertEquals(size, query.size);
        assertNull(query.aggs);
    }

    @Test
    void should_not_map_facets_to_query_given_contributors() {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final int from = 0;
        final int size = 10;

        // When
        searchRepository.searchAll(keyword, SearchItemType.CONTRIBUTOR, null, from, size);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString>> elasticSearchQueryArgumentCaptor =
                ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
        final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                classArgumentCaptor.capture());
        assertEquals("/contributors/_search", stringArgumentCaptor.getValue());
        final SearchRepository.ElasticSearchQuery<SearchRepository.SimpleQueryString> query = elasticSearchQueryArgumentCaptor.getValue();
        assertEquals(keyword, query.query.simpleQueryString.query);
        assertEquals(from, query.from);
        assertEquals(size, query.size);
        assertNull(query.aggs);
    }


    private void assertPagination(final int from, final int size, final int total, final int totalPage, final int totalItem, final int nextPageIndex,
                                  final boolean hasMore) throws JsonProcessingException {
        // Given
        final String keyword = faker.rickAndMorty().character();

        // When
        when(elasticSearchHttpClient.send("/_all/_search", HttpMethod.POST,
                SearchRepository.ElasticSearchQuery.<SearchRepository.SimpleQueryString>builder().from(from).size(size).query(SearchRepository.SimpleQueryString.builder().simpleQueryString(SearchRepository.Query.builder().query(keyword).build()).build()).build(), JsonNode.class)).thenReturn(Optional.of(objectMapper.readTree("""
                {
                    "took": 13,
                    "timed_out": false,
                    "_shards": {
                        "total": 1,
                        "successful": 1,
                        "skipped": 0,
                        "failed": 0
                    },
                    "hits": {
                        "total": {
                            "value": %s,
                            "relation": "eq"
                        },
                        "max_score": 3.916677,
                        "hits": [
                            {
                                "_index": "projects",
                                "_id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                "_score": 3.916677,
                                "_source": {
                                    "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                    "name": "DogGPT",
                                    "slug": "doggpt",
                                    "shortDescription": "Chat GPT for cat lovers",
                                    "longDescription": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                                    "languages": null,
                                    "ecosystems": [
                                        {
                                            "name": "Ethereum"
                                        }
                                    ],
                                    "categories": null
                                }
                            }
                        ]
                    }
                }
                """.formatted(total))));
        final SearchResponse searchResponse = searchRepository.searchAll(keyword, null, null, from, size);

        // Then
        assertNotNull(searchResponse);
        assertEquals(totalPage, searchResponse.getTotalPageNumber());
        assertEquals(totalItem, searchResponse.getTotalItemNumber());
        assertEquals(nextPageIndex, searchResponse.getNextPageIndex());
        assertEquals(hasMore, searchResponse.getHasMore());
    }
}
