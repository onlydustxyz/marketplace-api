package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import onlydust.com.marketplace.api.contract.model.ContributorAdvancedSearchResponse;
import onlydust.com.marketplace.api.contract.model.ProjectAdvancedSearchResponse;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import onlydust.com.marketplace.api.contract.model.SuggestResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ElasticSearchResponseMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    class ShouldMapSearchResponse {

        @Test
        void given_empty_result() throws JsonProcessingException {
            // Given
            final JsonNode jsonNode = objectMapper.readTree("""
                    {
                      "took": 1,
                      "timed_out": false,
                      "_shards": {
                        "total": 2,
                        "successful": 2,
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
                    """);

            // When
            final SearchResponse searchResponse = ElasticSearchResponseMapper.jsonNodeToSearchResponse(jsonNode, 0, 10);

            // Then
            assertEquals(0, searchResponse.getTotalItemNumber());
            assertEquals(0, searchResponse.getTotalPageNumber());
            assertEquals(false, searchResponse.getHasMore());
            assertEquals(0, searchResponse.getNextPageIndex());
            assertTrue(searchResponse.getResults().isEmpty());
            assertNull(searchResponse.getProjectFacets());
            assertNull(searchResponse.getTypeFacets());
        }

        @Test
        void given_products() throws JsonProcessingException {
            // Given
            final JsonNode jsonNode = objectMapper.readTree("""
                    {
                       "took": 1,
                       "timed_out": false,
                       "_shards": {
                         "total": 1,
                         "successful": 1,
                         "skipped": 0,
                         "failed": 0
                       },
                       "hits": {
                         "total": {
                           "value": 4,
                           "relation": "eq"
                         },
                         "max_score": 1,
                         "hits": [
                           {
                             "_index": "od-projects",
                             "_id": "e00ea16f-8a65-4790-8c3a-faed6abf8e8f",
                             "_score": 1,
                             "_source": {
                               "id": "e00ea16f-8a65-4790-8c3a-faed6abf8e8f",
                               "slug": "onlydust",
                               "name": "OnlyDust",
                               "shortDescription": "OD public repos 22314",
                               "languages": [
                                 {
                                   "name": "Typescript"
                                 },
                                 {
                                   "name": "Python"
                                 },
                                 {
                                   "name": "Rust"
                                 },
                                 {
                                   "name": "Javascript"
                                 },
                                 {
                                   "name": "Java"
                                 }
                               ],
                               "ecosystems": [
                                 {
                                   "name": "Aztec"
                                 }
                               ],
                               "categories": [
                                 {
                                   "name": "Devtool"
                                 }
                               ],
                               "contributorCount": 24,
                               "starCount": 26,
                               "forkCount": 19
                             }
                           }
                         ]
                       },
                       "aggregations": {
                         "ecosystems_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "Aztec",
                               "doc_count": 10
                             },
                             {
                               "key": "Lava",
                               "doc_count": 5
                             }
                           ]
                         },
                         "languages_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "Javascript",
                               "doc_count": 3
                             },
                             {
                               "key": "Typescript",
                               "doc_count": 2
                             }
                           ]
                         },
                         "categories_facet": {
                           "doc_count_error_upper_bound": 0,
                           "sum_other_doc_count": 0,
                           "buckets": [
                             {
                               "key": "Devtool",
                               "doc_count": 50
                             },
                             {
                               "key": "DeFi",
                               "doc_count": 40
                             }
                           ]
                         }
                       }
                     }
                    """);

            // When
            final SearchResponse searchResponse = ElasticSearchResponseMapper.jsonNodeToSearchResponse(jsonNode, 0, 10);

            // Then
            assertEquals(4, searchResponse.getTotalItemNumber());
            assertEquals(1, searchResponse.getTotalPageNumber());
            assertEquals(false, searchResponse.getHasMore());
            assertEquals(0, searchResponse.getNextPageIndex());
            assertEquals(1, searchResponse.getResults().size());

            final ProjectAdvancedSearchResponse firstResult = searchResponse.getResults().get(0).getProject();
            assertEquals("e00ea16f-8a65-4790-8c3a-faed6abf8e8f", firstResult.getId().toString());
            assertEquals("onlydust", firstResult.getSlug());
            assertEquals("OnlyDust", firstResult.getName());
            assertEquals("OD public repos 22314", firstResult.getShortDescription());
            assertEquals(List.of("Typescript", "Python", "Rust", "Javascript", "Java"), firstResult.getLanguages());
            assertEquals(List.of("Aztec"), firstResult.getEcosystems());
            assertEquals(List.of("Devtool"), firstResult.getCategories());
            assertEquals(24, firstResult.getContributorCount());
            assertEquals(26, firstResult.getStarCount());
            assertEquals(19, firstResult.getForkCount());

            assertEquals(10, searchResponse.getProjectFacets().getEcosystems().get(0).getCount());
            assertEquals("Aztec", searchResponse.getProjectFacets().getEcosystems().get(0).getName());
            assertEquals(5, searchResponse.getProjectFacets().getEcosystems().get(1).getCount());
            assertEquals("Lava", searchResponse.getProjectFacets().getEcosystems().get(1).getName());

            assertEquals(3, searchResponse.getProjectFacets().getLanguages().get(0).getCount());
            assertEquals("Javascript", searchResponse.getProjectFacets().getLanguages().get(0).getName());
            assertEquals(2, searchResponse.getProjectFacets().getLanguages().get(1).getCount());
            assertEquals("Typescript", searchResponse.getProjectFacets().getLanguages().get(1).getName());

            assertEquals(50, searchResponse.getProjectFacets().getCategories().get(0).getCount());
            assertEquals("Devtool", searchResponse.getProjectFacets().getCategories().get(0).getName());
            assertEquals(40, searchResponse.getProjectFacets().getCategories().get(1).getCount());
            assertEquals("DeFi", searchResponse.getProjectFacets().getCategories().get(1).getName());
        }

        @Test
        void given_all() throws JsonProcessingException {
            // Given
            final JsonNode jsonNode = objectMapper.readTree("""
                    {
                      "took": 3,
                      "timed_out": false,
                      "_shards": {
                        "total": 2,
                        "successful": 2,
                        "skipped": 0,
                        "failed": 0
                      },
                      "hits": {
                        "total": {
                          "value": 41,
                          "relation": "eq"
                        },
                        "max_score": 1,
                        "hits": [
                          {
                            "_index": "od-contributors",
                            "_id": "4464295",
                            "_score": 1,
                            "_source": {
                              "githubId": 4464295,
                              "githubLogin": "XAMPPRocky",
                              "bio": "➣ Livia Prima",
                              "contributionCount": 0,
                              "projectCount": 0,
                              "pullRequestCount": 0,
                              "issueCount": 0,
                              "htmlUrl": "https://github.com/XAMPPRocky"
                            }
                          },
                          {
                            "_index": "od-projects",
                            "_id": "f758f2c0-d3bb-4fba-8b17-f2a7c3f2eee9",
                            "_score": 1,
                            "_source": {
                              "id": "f758f2c0-d3bb-4fba-8b17-f2a7c3f2eee9",
                              "slug": "elmex",
                              "name": "Elmex",
                              "shortDescription": "Elmex anti caries professional 256799999887799 AABBCCDDEE 2224",
                              "languages": null,
                              "ecosystems": [
                                {
                                  "name": "NEAR"
                                },
                                {
                                  "name": "Lava"
                                }
                              ],
                              "categories": null,
                              "contributorCount": 4,
                              "starCount": 1,
                              "forkCount": 0
                            }
                          }
                        ]
                      },
                      "aggregations": {
                        "by_categories_name": {
                          "doc_count_error_upper_bound": 0,
                          "sum_other_doc_count": 0,
                          "buckets": [
                            {
                              "key": "Devtool",
                              "doc_count": 2
                            }
                          ]
                        },
                        "by_languages_name": {
                          "doc_count_error_upper_bound": 0,
                          "sum_other_doc_count": 0,
                          "buckets": [
                            {
                              "key": "Javascript",
                              "doc_count": 2
                            }
                          ]
                        },
                        "indexes_facet": {
                          "doc_count_error_upper_bound": 0,
                          "sum_other_doc_count": 0,
                          "buckets": [
                            {
                              "key": "od-contributors",
                              "doc_count": 37
                            },
                            {
                              "key": "od-projects",
                              "doc_count": 4
                            }
                          ]
                        },
                        "by_ecosystems_name": {
                          "doc_count_error_upper_bound": 0,
                          "sum_other_doc_count": 0,
                          "buckets": [
                            {
                              "key": "Aztec",
                              "doc_count": 1
                            }
                          ]
                        }
                      }
                    }
                    """);

            // When
            final SearchResponse searchResponse = ElasticSearchResponseMapper.jsonNodeToSearchResponse(jsonNode, 0, 2);

            // Then
            assertEquals(2, searchResponse.getTypeFacets().getTypes().size());
            assertEquals("Contributors", searchResponse.getTypeFacets().getTypes().get(0).getName());
            assertEquals(37, searchResponse.getTypeFacets().getTypes().get(0).getCount());
            assertEquals("Projects", searchResponse.getTypeFacets().getTypes().get(1).getName());
            assertEquals(4, searchResponse.getTypeFacets().getTypes().get(1).getCount());
            assertNotNull(searchResponse.getResults().get(0).getContributor());
            assertNull(searchResponse.getResults().get(0).getProject());
            assertNotNull(searchResponse.getResults().get(1).getProject());
            assertNull(searchResponse.getResults().get(1).getContributor());
            final ContributorAdvancedSearchResponse contributor = searchResponse.getResults().get(0).getContributor();
            assertEquals(4464295L, contributor.getGithubId());
            assertEquals("XAMPPRocky", contributor.getGithubLogin());
            assertEquals("➣ Livia Prima", contributor.getBio());
            assertEquals(0, contributor.getContributionCount());
            assertEquals(0, contributor.getProjectCount());
            assertEquals(0, contributor.getPullRequestCount());
            assertEquals(0, contributor.getIssueCount());
            assertEquals("https://github.com/XAMPPRocky", contributor.getHtmlUrl());
        }

        @Test
        void given_pagination() {
            assertPaginationEquals(0, 10, 9, 1, 9, 0, false);
            assertPaginationEquals(0, 10, 12, 2, 12, 1, true);
            assertPaginationEquals(5, 5, 12, 3, 12, 2, true);
            assertPaginationEquals(5, 10, 5, 1, 5, 0, false);
        }

        private void assertPaginationEquals(final int from, final int size, final int total, final int totalPage, final int totalItem, final int nextPageIndex,
                                            final boolean hasMore) {
            final SearchResponse searchResponse = new SearchResponse();
            ElasticSearchResponseMapper.mapPagination(from, size, total, searchResponse);
            assertEquals(totalPage, searchResponse.getTotalPageNumber());
            assertEquals(totalItem, searchResponse.getTotalItemNumber());
            assertEquals(nextPageIndex, searchResponse.getNextPageIndex());
            assertEquals(hasMore, searchResponse.getHasMore());
        }
    }

    @Nested
    public class ShouldMapSuggestResponse {

        @Test
        void given_a_contributor() throws JsonProcessingException {
            // Given
            // When
            final JsonNode jsonNode = objectMapper.readTree("""
                    {
                      "took": 2,
                      "timed_out": false,
                      "_shards": {
                        "total": 2,
                        "successful": 2,
                        "skipped": 0,
                        "failed": 0
                      },
                      "hits": {
                        "total": {
                          "value": 41,
                          "relation": "eq"
                        },
                        "max_score": 1,
                        "hits": [
                          {
                            "_index": "od-contributors",
                            "_id": "34384633",
                            "_score": 1,
                            "_source": {
                              "githubId": 34384633,
                              "githubLogin": "tdelabro",
                              "bio": null,
                              "contributionCount": 173,
                              "projectCount": 1,
                              "pullRequestCount": 118,
                              "issueCount": 0,
                              "htmlUrl": "https://github.com/tdelabro"
                            }
                          }
                        ]
                      }
                    }
                    """);

            final SuggestResponse suggestResponse = ElasticSearchResponseMapper.jsonNodeToSuggestResponse(jsonNode);

            // Then
            assertEquals("tdelabro", suggestResponse.getValue());
        }

         @Test
        void given_a_project() throws JsonProcessingException {
            // Given
            // When
            final JsonNode jsonNode = objectMapper.readTree("""
                    {
                      "took": 2,
                      "timed_out": false,
                      "_shards": {
                        "total": 2,
                        "successful": 2,
                        "skipped": 0,
                        "failed": 0
                      },
                      "hits": {
                        "total": {
                          "value": 41,
                          "relation": "eq"
                        },
                        "max_score": 1,
                        "hits": [
                          {
                           "_index": "od-projects",
                           "_id": "f758f2c0-d3bb-4fba-8b17-f2a7c3f2eee9",
                           "_score": 1,
                           "_source": {
                             "id": "f758f2c0-d3bb-4fba-8b17-f2a7c3f2eee9",
                             "slug": "elmex",
                             "name": "Elmex",
                             "shortDescription": "Elmex anti caries professional 256799999887799 AABBCCDDEE 2224",
                             "languages": null,
                             "ecosystems": [
                               {
                                 "name": "NEAR"
                               },
                               {
                                 "name": "Lava"
                               }
                             ],
                             "categories": null,
                             "contributorCount": 4,
                             "starCount": 1,
                             "forkCount": 0
                           }
                         }
                        ]
                      }
                    }
                    """);

            final SuggestResponse suggestResponse = ElasticSearchResponseMapper.jsonNodeToSuggestResponse(jsonNode);

            // Then
            assertEquals("Elmex", suggestResponse.getValue());
        }


    }
}
