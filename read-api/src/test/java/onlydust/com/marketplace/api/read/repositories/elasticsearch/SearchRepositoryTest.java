package onlydust.com.marketplace.api.read.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchHttpClient;
import io.netty.handler.codec.http.HttpMethod;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.repositories.elasticsearch.SearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.api.read.repositories.elasticsearch.SearchRepository.buildSuggestQuery;
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

    @Nested
    public class ShouldSearch {
        @Test
        void should_map_empty_response() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();
            final int from = faker.number().numberBetween(0, 1000);
            final int size = faker.number().numberBetween(0, 1000);

            // When
            when(elasticSearchHttpClient.send("/_all/_search", HttpMethod.POST,
                    SearchRepository.ElasticSearchQuery.<SearchRepository.QueryString>builder()
                            .from(from).size(size).query(SearchRepository.QueryString.builder()
                                    .queryString(SearchRepository.Query.builder()
                                            .query(keyword).build()).build()).build(), JsonNode.class))
                    .thenReturn(Optional.of(objectMapper.readTree("""
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
        void should_map_simple_project_query() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();
            final int from = 0;
            final int size = 10;

            // When
            when(elasticSearchHttpClient.send("/od-*/_search", HttpMethod.POST,
                    SearchRepository.ElasticSearchQuery.<SearchRepository.QueryString>builder()
                            .from(from)
                            .size(size)
                            .query(SearchRepository.QueryString.fromKeyword(keyword))
                            .build(), JsonNode.class))
                    .thenReturn(Optional.of(objectMapper.readTree("""
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
                                            "_index": "od-projects",
                                            "_id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                            "_score": 3.916677,
                                            "_source": {
                                                "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                                "name": "DogGPT",
                                                "slug": "doggpt",
                                                "shortDescription": "Chat GPT for cat lovers",
                                                "languages": null,
                                                "forkCount": 13,
                                                "starCount": 234,
                                                "contributorCount": 432,
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
            assertEquals(13, searchItemResponse.getProject().getForkCount());
            assertEquals(234, searchItemResponse.getProject().getStarCount());
            assertEquals(432, searchItemResponse.getProject().getContributorCount());
        }

        @Test
        void should_map_simple_contributor_query() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();
            final int from = 0;
            final int size = 10;

            // When
            when(elasticSearchHttpClient.send("/od-contributors/_search", HttpMethod.POST,
                    SearchRepository.ElasticSearchQuery.<SearchRepository.QueryString>builder()
                            .from(from)
                            .size(size)
                            .query(SearchRepository.QueryString.fromKeyword(keyword))
                            .build(), JsonNode.class))
                    .thenReturn(Optional.of(objectMapper.readTree("""
                            {
                              "took": 39,
                              "timed_out": false,
                              "_shards": {
                                "total": 1,
                                "successful": 1,
                                "skipped": 0,
                                "failed": 0
                              },
                              "hits": {
                                "total": {
                                  "value": 10000,
                                  "relation": "gte"
                                },
                                "max_score": 1.0,
                                "hits": [
                                  {
                                    "_index": "od-contributors",
                                    "_id": "117",
                                    "_score": 1.0,
                                    "_source": {
                                      "githubId": 117,
                                      "githubLogin": "grempe",
                                      "bio": "CEO @truestamp\\r\\n; interested in coding, cryptography, data security & integrity: ex silicon valley startups VP Eng, ex @accenture",
                                      "contributionCount": 1,
                                      "projectCount": 2,
                                      "pullRequestCount": 3,
                                      "issueCount": 4,
                                      "htmlUrl": "https://github.com/grempe"
                                    }
                                  }
                                ]
                              }
                            }
                            """)));
            final SearchResponse searchResponse = searchRepository.searchAll(keyword, SearchItemType.CONTRIBUTOR, null, from, size);

            // Then
            assertNotNull(searchResponse);
            final SearchItemResponse searchItemResponse = searchResponse.getResults().get(0);
            assertEquals(117L, searchItemResponse.getContributor().getGithubId());
            assertEquals("grempe", searchItemResponse.getContributor().getGithubLogin());
            assertEquals("CEO @truestamp\r\n; interested in coding, cryptography, data security & integrity: ex silicon valley startups VP Eng, ex @accenture",
                    searchItemResponse.getContributor().getBio());
            assertEquals(1, searchItemResponse.getContributor().getContributionCount());
            assertEquals(2, searchItemResponse.getContributor().getProjectCount());
            assertEquals(4, searchItemResponse.getContributor().getIssueCount());
            assertEquals(3, searchItemResponse.getContributor().getPullRequestCount());
            assertEquals("https://github.com/grempe", searchItemResponse.getContributor().getHtmlUrl());

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
            final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.QueryString>> elasticSearchQueryArgumentCaptor =
                    ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
            final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
            final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
            verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                    classArgumentCaptor.capture());
            assertEquals("/od-projects/_search", stringArgumentCaptor.getValue());
            final SearchRepository.ElasticSearchQuery<SearchRepository.QueryString> query = elasticSearchQueryArgumentCaptor.getValue();
            assertEquals("*%s*".formatted(keyword), query.getQuery().getQueryString().getQuery());
            assertEquals(from, query.getFrom());
            assertEquals(size, query.getSize());
            assertEquals("""
                    {
                      "ecosystems_facet" : {
                        "terms" : {
                          "field" : "ecosystems.name.keyword"
                        }
                      },
                      "languages_facet" : {
                        "terms" : {
                          "field" : "languages.name.keyword"
                        }
                      },
                      "categories_facet" : {
                        "terms" : {
                          "field" : "categories.name.keyword"
                        }
                      }
                    }""", query.getAggs().toPrettyString());
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
            final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.QueryString>> elasticSearchQueryArgumentCaptor =
                    ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
            final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
            final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
            verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                    classArgumentCaptor.capture());
            assertEquals("/od-*/_search", stringArgumentCaptor.getValue());
            final SearchRepository.ElasticSearchQuery<SearchRepository.QueryString> query = elasticSearchQueryArgumentCaptor.getValue();
            assertEquals("*%s*".formatted(keyword), query.getQuery().getQueryString().getQuery());
            assertEquals(from, query.getFrom());
            assertEquals(size, query.getSize());
            assertNull(query.getAggs());
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
            final ArgumentCaptor<SearchRepository.ElasticSearchQuery<SearchRepository.QueryString>> elasticSearchQueryArgumentCaptor =
                    ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
            final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
            final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
            verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), elasticSearchQueryArgumentCaptor.capture(),
                    classArgumentCaptor.capture());
            assertEquals("/od-contributors/_search", stringArgumentCaptor.getValue());
            final SearchRepository.ElasticSearchQuery<SearchRepository.QueryString> query = elasticSearchQueryArgumentCaptor.getValue();
            assertEquals("*%s*".formatted(keyword), query.getQuery().getQueryString().getQuery());
            assertEquals(from, query.getFrom());
            assertEquals(size, query.getSize());
            assertNull(query.getAggs());
        }


        @Test
        void should_map_project_aggregations_to_facets() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();
            final int from = 0;
            final int size = 10;

            // When
            final SearchRepository.ElasticSearchQuery<SearchRepository.QueryString> query =
                    SearchRepository.ElasticSearchQuery.<SearchRepository.QueryString>builder()
                            .from(from)
                            .size(size)
                            .query(SearchRepository.QueryString.fromKeyword(keyword))
                            .build().withFacets();
            when(elasticSearchHttpClient.send("/od-projects/_search", HttpMethod.POST,
                    query, JsonNode.class)).thenReturn(Optional.of(objectMapper.readTree("""
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
                             "_index": "od-projects",
                             "_id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                             "_score": 3.916677,
                             "_source": {
                               "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                               "name": "DogGPT",
                               "slug": "doggpt",
                               "shortDescription": "Chat GPT for cat lovers",
                               "languages": [
                                 {
                                   "name": "Java"
                                 }
                               ],
                               "ecosystems": [
                                 {
                                   "name": "Ethereum"
                                 }
                               ],
                               "categories": [
                                 {
                                   "name": "Web3"
                                 }
                               ]
                             }
                           }
                         ]
                       },
                       "aggregations": {
                         "languages_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "Cairo",
                               "doc_count": 23
                             },
                             {
                               "key": "Typescript",
                               "doc_count": 4
                             }
                           ]
                         },
                         "ecosystems_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "Starknet",
                               "doc_count": 40
                             },
                             {
                               "key": "Ethereum",
                               "doc_count": 10
                             }
                           ]
                         },
                         "categories_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "AI",
                               "doc_count": 210
                             },
                             {
                               "key": "Web3",
                               "doc_count": 19
                             }
                           ]
                         }
                       }
                     }
                    """)));
            final SearchResponse searchResponse = searchRepository.searchAll(keyword, SearchItemType.PROJECT, null, from, size);

            // Then
            final List<SearchFacetResponse> facets = searchResponse.getFacets();
            final Map<SearchFacetType, List<SearchFacetResponse>> returnedFacetsByType =
                    facets.stream().collect(Collectors.groupingBy(SearchFacetResponse::getType));
            assertEquals(23, returnedFacetsByType.get(SearchFacetType.LANGUAGE).get(0).getCount());
            assertEquals(4, returnedFacetsByType.get(SearchFacetType.LANGUAGE).get(1).getCount());
            assertEquals(40, returnedFacetsByType.get(SearchFacetType.ECOSYSTEM).get(0).getCount());
            assertEquals(10, returnedFacetsByType.get(SearchFacetType.ECOSYSTEM).get(1).getCount());
            assertEquals(210, returnedFacetsByType.get(SearchFacetType.CATEGORY).get(0).getCount());
            assertEquals(19, returnedFacetsByType.get(SearchFacetType.CATEGORY).get(1).getCount());
        }


        private void assertPagination(final int from, final int size, final int total, final int totalPage, final int totalItem, final int nextPageIndex,
                                      final boolean hasMore) throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();

            // When
            when(elasticSearchHttpClient.send("/od-*/_search", HttpMethod.POST,
                    SearchRepository.ElasticSearchQuery.<SearchRepository.QueryString>builder()
                            .from(from)
                            .size(size)
                            .query(SearchRepository.QueryString.fromKeyword(keyword))
                            .build(), JsonNode.class)).thenReturn(Optional.of(objectMapper.readTree("""
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
                                    "_index": "od-projects",
                                    "_id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                    "_score": 3.916677,
                                    "_source": {
                                        "id": "61ef7d3a-81a2-4baf-bdb0-e7ae5e165d17",
                                        "name": "DogGPT",
                                        "slug": "doggpt",
                                        "shortDescription": "Chat GPT for cat lovers",
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


    @Nested
    public class ShouldSuggest {


        @Test
        void should_suggest_given_queries() {
            // Given
            final String keyword = faker.rickAndMorty().character();
            assertSuggestQuery(keyword, "/od-projects/_search", SearchItemType.PROJECT, """
                    {
                      "prefix" : {
                        "name" : "%s"
                      }
                    }""".formatted(keyword));
            assertSuggestQuery(keyword, "/od-contributors/_search", SearchItemType.CONTRIBUTOR, """
                    {
                      "prefix" : {
                        "githubLogin" : "%s"
                      }
                    }""".formatted(keyword));
            assertSuggestQuery(keyword, "/od-*/_search", null, """
                    {
                      "bool" : {
                        "should" : [ {
                          "prefix" : {
                            "name" : "%s"
                          }
                        }, {
                          "prefix" : {
                            "githubLogin" : "%s"
                          }
                        } ]
                      }
                    }""".formatted(keyword, keyword));
        }

        private void assertSuggestQuery(String keyword, String path, SearchItemType searchItemType, String expectedQuery) {
            Mockito.reset(elasticSearchHttpClient);
            final ArgumentCaptor<SearchRepository.ElasticSearchQuery<JsonNode>> requestBody =
                    ArgumentCaptor.forClass(SearchRepository.ElasticSearchQuery.class);
            final ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
            final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);

            // When
            final SuggestResponse suggest = searchRepository.suggest(keyword, searchItemType);

            // Then
            verify(elasticSearchHttpClient).send(stringArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(),
                    requestBody.capture(), classArgumentCaptor.capture());
            assertNotNull(suggest);
            assertEquals(path, stringArgumentCaptor.getValue());
            assertEquals(HttpMethod.POST, httpMethodArgumentCaptor.getValue());
            assertEquals(JsonNode.class, classArgumentCaptor.getValue());
            assertEquals(expectedQuery, requestBody.getValue().getQuery().toPrettyString());
            assertEquals(0, requestBody.getValue().getFrom());
            assertEquals(1, requestBody.getValue().getSize());
        }

        @Test
        void should_suggest_given_a_project_response() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();

            // When
            when(elasticSearchHttpClient.send("/od-*/_search", HttpMethod.POST, buildSuggestQuery(keyword, null), JsonNode.class))
                    .thenReturn(Optional.of(objectMapper.readTree("""
                            {
                              "took": 4,
                              "timed_out": false,
                              "_shards": {
                                "total": 2,
                                "successful": 2,
                                "skipped": 0,
                                "failed": 0
                              },
                              "hits": {
                                "total": {
                                  "value": 6,
                                  "relation": "eq"
                                },
                                "max_score": 1,
                                "hits": [
                                  {
                                    "_index": "od-projects",
                                    "_id": "e55c5843-66b9-4e6c-b5dc-eef8729b286b",
                                    "_score": 1,
                                    "_source": {
                                      "id": "e55c5843-66b9-4e6c-b5dc-eef8729b286b",
                                      "slug": "symeo",
                                      "name": "Symeo",
                                      "shortDescription": "Best project ever",
                                      "languages": [
                                        {
                                          "name": "Typescript"
                                        },
                                        {
                                          "name": "Go"
                                        },
                                        {
                                          "name": "Javascript"
                                        }
                                      ],
                                      "ecosystems": null,
                                      "categories": [
                                        {
                                          "name": "Devtool"
                                        }
                                      ],
                                      "contributorCount": 3,
                                      "starCount": 7,
                                      "forkCount": 1
                                    }
                                  }
                                ]
                              }
                            }
                            """)));
            final SuggestResponse suggest = searchRepository.suggest(keyword, null);

            // Then
            assertNotNull(suggest);
            assertEquals("Symeo", suggest.getValue());
        }

        void should_suggest_given_a_contributor_response() throws JsonProcessingException {
            // Given
            final String keyword = faker.rickAndMorty().character();

            // When
            when(elasticSearchHttpClient.send("/od-*/_search", HttpMethod.POST, buildSuggestQuery(keyword, null), JsonNode.class))
                    .thenReturn(Optional.of(objectMapper.readTree("""
                            {
                              "took": 4,
                              "timed_out": false,
                              "_shards": {
                                "total": 2,
                                "successful": 2,
                                "skipped": 0,
                                "failed": 0
                              },
                              "hits": {
                                "total": {
                                  "value": 6,
                                  "relation": "eq"
                                },
                                "max_score": 1,
                                "hits": [
                                  {
                                           "_index": "od-contributors",
                                           "_id": "136718082",
                                           "_score": 1,
                                           "_source": {
                                             "githubId": 136718082,
                                             "githubLogin": "od-develop",
                                             "bio": null,
                                             "contributionCount": 0,
                                             "projectCount": 0,
                                             "pullRequestCount": 0,
                                             "issueCount": 0,
                                             "htmlUrl": "https://github.com/od-develop"
                                           }
                                         }
                                ]
                              }
                            }
                            """)));
            final SuggestResponse suggest = searchRepository.suggest(keyword, null);

            // Then
            assertNotNull(suggest);
            assertEquals("od-develop", suggest.getValue());
        }
    }

}
