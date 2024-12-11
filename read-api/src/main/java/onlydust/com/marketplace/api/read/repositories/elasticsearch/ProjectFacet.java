package onlydust.com.marketplace.api.read.repositories.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ProjectFacet {
    ECOSYSTEMS("ecosystems_facet", "ecosystems.name.keyword"),
    LANGUAGES("languages_facet", "languages.name.keyword"),
    CATEGORIES("categories_facet", "categories.name.keyword");

    private final String aggregationName;
    private final String field;

    public static List<ElasticSearchQuery.Aggregation> toAggregations() {
        return Arrays.stream(ProjectFacet.values()).map(ProjectFacet::toAggregation).collect(Collectors.toList());
    }

    private ElasticSearchQuery.Aggregation toAggregation() {
        return ElasticSearchQuery.Aggregation.builder()
                .name(this.aggregationName)
                .field(this.field)
                .build();
    }
}
