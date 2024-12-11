package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ElasticSearchQueryTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Faker faker = new Faker();

    @Test
    void should_map_pagination() {
        // Given
        final int from = 0;
        final int size = 10;
        final String expectedQuery = """
                {
                    "from": 0,
                    "size": 10
                }
                """;

        // When
        final JsonNode query = ElasticSearchQuery.builder().withPagination(from, size).build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_query_string() {
        // Given
        final String keyword = faker.rickAndMorty().character();
        final String expectedQuery = """
                {
                    "query": {
                       "query_string": {
                         "query": "*%s*"
                       }
                     }
                }
                """.formatted(keyword);

        // When
        final JsonNode query = ElasticSearchQuery.builder().withQueryString(keyword).build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_prefix_query() {
        // Given
        final String field = faker.rickAndMorty().character();
        final String keyword = faker.rickAndMorty().location();
        final String expectedQuery = """
                {
                    "query": {
                       "prefix": {
                         "%s": "%s"
                       }
                     }
                }
                """.formatted(field, keyword);

        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withPrefixes(ElasticSearchQuery.Prefix.builder()
                        .field(field)
                        .keyword(keyword)
                        .build())
                .build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }


    @Test
    void should_map_multiple_prefix_query() {
        // Given
        final ElasticSearchQuery.Prefix prefix1 = ElasticSearchQuery.Prefix.builder()
                .field(faker.rickAndMorty().character())
                .keyword(faker.rickAndMorty().location())
                .build();
        final ElasticSearchQuery.Prefix prefix2 = ElasticSearchQuery.Prefix.builder()
                .field(faker.lordOfTheRings().character())
                .keyword(faker.lordOfTheRings().location())
                .build();
        final String expectedQuery = """
                {
                    "query": {
                        "bool": {
                          "should": [
                            {
                              "prefix": {
                                "%s": "%s"
                              }
                            },
                            {
                              "prefix": {
                                "%s": "%s"
                              }
                            }
                          ]
                        }
                      }
                }
                """.formatted(prefix1.field, prefix1.keyword, prefix2.field, prefix2.keyword);

        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withPrefixes(prefix1, prefix2)
                .build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_multiple_term() {
        // Given
        final String term1 = faker.rickAndMorty().character();
        final String term2 = faker.rickAndMorty().location();
        final String field = faker.lordOfTheRings().character();
        final String expectedQuery = """
                {
                    "query": {
                       "terms": {
                         "%s": ["%s", "%s"]
                       }
                     }
                }
                """.formatted(field, term1, term2);

        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withMultipleTerms(ElasticSearchQuery.MultipleTerm.builder()
                        .field(field)
                        .terms(List.of(term1, term2))
                        .build())
                .build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_multiple_terms() {
        // Given
        final ElasticSearchQuery.MultipleTerm multipleTerm1 = ElasticSearchQuery.MultipleTerm.builder()
                .field(faker.rickAndMorty().character())
                .terms(List.of(faker.pokemon().name(), faker.pokemon().location()))
                .build();
        final ElasticSearchQuery.MultipleTerm multipleTerm2 = ElasticSearchQuery.MultipleTerm.builder()
                .field(faker.rickAndMorty().location())
                .terms(List.of(faker.lordOfTheRings().location(), faker.lordOfTheRings().character()))
                .build();
        final String expectedQuery = """
                {
                    "query": {
                          "bool": {
                            "must": [
                              {
                                "terms": {
                                  "%s": [
                                    "%s", "%s"
                                  ]
                                }
                              },
                              {
                                "terms": {
                                  "%s": [
                                    "%s", "%s"
                                  ]
                                }
                              }
                            ]
                          }
                        }
                }
                """.formatted(multipleTerm1.field, multipleTerm1.terms.get(0), multipleTerm1.terms.get(1),
                multipleTerm2.field, multipleTerm2.terms.get(0), multipleTerm2.terms.get(1));

        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withMultipleTerms(multipleTerm1, multipleTerm2)
                .build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_multiple_terms_with_query_string() {
        // Given
        final ElasticSearchQuery.MultipleTerm multipleTerm1 = ElasticSearchQuery.MultipleTerm.builder()
                .field(faker.rickAndMorty().character())
                .terms(List.of(faker.pokemon().name(), faker.pokemon().location()))
                .build();
        final ElasticSearchQuery.MultipleTerm multipleTerm2 = ElasticSearchQuery.MultipleTerm.builder()
                .field(faker.rickAndMorty().location())
                .terms(List.of(faker.lordOfTheRings().location(), faker.lordOfTheRings().character()))
                .build();
        final String keyword = faker.harryPotter().character();
        final String expectedQuery = """
                {
                    "query": {
                          "bool": {
                            "must": [
                              {
                                "query_string": {
                                  "query": "*%s*"
                                }
                              },
                              {
                                "terms": {
                                  "%s": [
                                    "%s", "%s"
                                  ]
                                }
                              },
                              {
                                "terms": {
                                  "%s": [
                                    "%s", "%s"
                                  ]
                                }
                              }
                            ]
                          }
                        }
                }
                """.formatted(keyword, multipleTerm1.field, multipleTerm1.terms.get(0), multipleTerm1.terms.get(1),
                multipleTerm2.field, multipleTerm2.terms.get(0), multipleTerm2.terms.get(1));

        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withMultipleTerms(multipleTerm1, multipleTerm2)
                .withQueryString(keyword)
                .build();

        // Then
        assertQueryEquals(expectedQuery, query);
    }

    @Test
    void should_map_aggregations() {
        // Given
        final ElasticSearchQuery.Aggregation aggregation1 = ElasticSearchQuery.Aggregation.builder()
                .name(faker.harryPotter().location())
                .field(faker.harryPotter().book())
                .build();
        final ElasticSearchQuery.Aggregation aggregation2 = ElasticSearchQuery.Aggregation.builder()
                .name(faker.pokemon().name())
                .field(faker.pokemon().location())
                .build();


        // When
        final JsonNode query = ElasticSearchQuery.builder()
                .withAggregations(aggregation1, aggregation2)
                .build();

        // Then
        assertQueryEquals("""
                {
                  "aggs": {
                    "%s": {
                      "terms": {
                        "field": "%s"
                      }
                    },
                    "%s": {
                      "terms": {
                        "field": "%s"
                      }
                    }
                  }
                }""".formatted(aggregation1.name, aggregation1.field, aggregation2.name, aggregation2.field), query);
    }

    private static void assertQueryEquals(String expectedQuery, JsonNode query) {
        try {
            Assertions.assertEquals(objectMapper.readTree(expectedQuery), query);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
