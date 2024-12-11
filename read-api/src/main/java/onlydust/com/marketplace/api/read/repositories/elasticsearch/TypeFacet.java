package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeFacet {
    INDEXES("indexes_facet", "_index");
    private final String aggregationName;
    private final String field;

    public ElasticSearchQuery.Aggregation toAggregation() {
        return ElasticSearchQuery.Aggregation.builder()
                .name(this.aggregationName)
                .field(this.field)
                .build();
    }
}
