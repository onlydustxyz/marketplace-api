package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

public class ElasticSearchQuery {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ElasticSearchQuery() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer from;
        private Integer size;
        private String queryStringKeyword;
        private List<Prefix> prefixes;
        private List<MultipleTerm> multipleTerms;
        private List<Aggregation> aggregations;

        public Builder withPagination(final int from, final int size) {
            this.from = from;
            this.size = size;
            return this;
        }

        public Builder withQueryString(final String keyword) {
            this.queryStringKeyword = keyword;
            return this;
        }

        public Builder withPrefixes(final Prefix... prefixes) {
            this.prefixes = Arrays.stream(prefixes).toList();
            return this;
        }

        public Builder withMultipleTerms(final MultipleTerm... multipleTerms) {
            this.multipleTerms = Arrays.stream(multipleTerms).toList();
            return this;
        }

        public Builder withAggregations(final Aggregation... aggregations) {
            this.aggregations = Arrays.stream(aggregations).toList();
            return this;
        }


        public JsonNode build() {
            final ObjectNode parentNode = objectMapper.createObjectNode();
            addPagination(parentNode);
            addQuery(parentNode);
            addAggregations(parentNode);
            return parentNode;
        }

        private void addAggregations(ObjectNode parentNode) {
            if (nonNull(aggregations)) {
                final ObjectNode aggsNode = objectMapper.createObjectNode();
                for (final Aggregation aggregation : aggregations) {
                    final ObjectNode fieldNode = objectMapper.createObjectNode();
                    final ObjectNode termsNode = objectMapper.createObjectNode();
                    fieldNode.put("field", aggregation.field);
                    termsNode.set("terms", fieldNode);
                    aggsNode.set(aggregation.name, termsNode);
                }
                parentNode.set("aggs", aggsNode);
            }
        }

        private void addQuery(ObjectNode parentNode) {
            if (nonNull(queryStringKeyword) && nonNull(multipleTerms)) {
                final List<ObjectNode> queriesToCombine = new ArrayList<>();
                queriesToCombine.add(buildQueryStringNode(queryStringKeyword));
                queriesToCombine.addAll(multipleTerms.stream().map(this::buildTermsNode).toList());
                parentNode.set("query", buildMustNode(queriesToCombine));
            } else if (nonNull(queryStringKeyword)) {
                parentNode.set("query", buildQueryStringNode(queryStringKeyword));
            } else if (nonNull(multipleTerms)) {
                if (multipleTerms.size() == 1) {
                    final MultipleTerm multipleTerm = multipleTerms.get(0);
                    final ObjectNode queryNode = buildTermsNode(multipleTerm);
                    parentNode.set("query", queryNode);
                } else {
                    parentNode.set("query", buildMustNode(multipleTerms.stream().map(this::buildTermsNode).toList()));
                }
            } else if (nonNull(prefixes)) {
                if (prefixes.size() == 1) {
                    final Prefix prefix = prefixes.get(0);
                    final ObjectNode queryNode = buildPrefixNode(prefix);
                    parentNode.set("query", queryNode);
                } else {
                    parentNode.set("query", buildShouldNode(prefixes.stream().map(this::buildPrefixNode).toList()));
                }
            }
        }

        private void addPagination(ObjectNode parentNode) {
            if (nonNull(from)) {
                parentNode.put("from", from);
            }
            if (nonNull(size)) {
                parentNode.put("size", size);
            }
        }

        private ObjectNode buildMustNode(final List<ObjectNode> queriesToCombine) {
            final ObjectNode queryNode = objectMapper.createObjectNode();
            final ObjectNode boolNode = objectMapper.createObjectNode();
            final ArrayNode mustArray = boolNode.putArray("must");
            for (ObjectNode query : queriesToCombine) {
                mustArray.add(query);
            }
            queryNode.set("bool", boolNode);
            return queryNode;
        }

        private ObjectNode buildShouldNode(final List<ObjectNode> queriesToCombine) {
            final ObjectNode queryNode = objectMapper.createObjectNode();
            final ObjectNode boolNode = objectMapper.createObjectNode();
            final ArrayNode shouldArray = boolNode.putArray("should");
            for (ObjectNode query : queriesToCombine) {
                shouldArray.add(query);
            }
            queryNode.set("bool", boolNode);
            return queryNode;
        }

        private ObjectNode buildTermsNode(final MultipleTerm multipleTerm) {
            final ObjectNode queryNode = objectMapper.createObjectNode();
            final ObjectNode termsNode = objectMapper.createObjectNode();
            final ArrayNode termsArray = termsNode.putArray(multipleTerm.field);
            for (String term : multipleTerm.terms) {
                termsArray.add(term);
            }
            queryNode.set("terms", termsNode);
            return queryNode;
        }

        private ObjectNode buildPrefixNode(final Prefix prefix) {
            final ObjectNode queryNode = objectMapper.createObjectNode();
            final ObjectNode prefixNode = objectMapper.createObjectNode();
            prefixNode.put(prefix.field, prefix.keyword);
            queryNode.set("prefix", prefixNode);
            return queryNode;
        }

        private ObjectNode buildQueryStringNode(final String keyword) {
            final ObjectNode queryNode = objectMapper.createObjectNode();
            final ObjectNode queryStringNode = objectMapper.createObjectNode();
            queryStringNode.put("query", "*%s*".formatted(queryStringKeyword));
            queryNode.set("query_string", queryStringNode);
            return queryNode;
        }
    }

    @lombok.Builder
    public static class Prefix {
        final String field;
        final String keyword;
    }

    @lombok.Builder
    public static class MultipleTerm {
        final List<String> terms;
        final String field;
    }

    @lombok.Builder
    public static class Aggregation {
        final String name;
        final String field;
    }
}
