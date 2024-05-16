package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.EcosystemView;
import onlydust.com.marketplace.project.domain.view.ProjectCardView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Immutable
public class ProjectPageItemFiltersQueryEntity {
    @Id
    UUID id;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Map<String, Long>> technologies;

    @EqualsAndHashCode
    public static class Ecosystem {
        @JsonProperty("url")
        String url;
        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
    }

    public static Map<String, Set<Object>> entitiesToFilters(final List<ProjectPageItemFiltersQueryEntity> filtersViewEntities) {
        final Map<String, Set<Object>> filters = new HashMap<>();
        final Set<String> technologyNames = new HashSet<>();
        final Set<EcosystemView> ecosystems = new HashSet<>();
        for (ProjectPageItemFiltersQueryEntity filtersViewEntity : filtersViewEntities) {
            if (nonNull(filtersViewEntity.technologies)) {
                filtersViewEntity.technologies.stream()
                        .filter(Objects::nonNull)
                        .map(Map::keySet)
                        .forEach(technologyNames::addAll);
            }
            if (nonNull(filtersViewEntity.ecosystems)) {
                filtersViewEntity.ecosystems.stream()
                        .filter(ecosystem -> nonNull(ecosystem.name))
                        .map(ecosystem -> EcosystemView.builder()
                                .id(ecosystem.id)
                                .url(ecosystem.url)
                                .logoUrl(ecosystem.logoUrl)
                                .name(ecosystem.name).build())
                        .forEach(ecosystems::add);
            }
        }
        filters.put(ProjectCardView.FilterBy.TECHNOLOGIES.name(),
                technologyNames.stream().map(Object.class::cast).collect(Collectors.toSet()));
        filters.put(ProjectCardView.FilterBy.ECOSYSTEMS.name(),
                ecosystems.stream().map(Object.class::cast).collect(Collectors.toSet()));
        return filters;
    }

}
