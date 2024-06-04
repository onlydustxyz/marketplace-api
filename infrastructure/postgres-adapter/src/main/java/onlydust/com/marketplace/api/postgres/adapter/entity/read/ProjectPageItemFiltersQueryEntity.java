package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.EcosystemView;
import onlydust.com.marketplace.project.domain.view.LanguageView;
import onlydust.com.marketplace.project.domain.view.ProjectCardView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@NoArgsConstructor
@Data
@Immutable
public class ProjectPageItemFiltersQueryEntity {
    @Id
    UUID id;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectPageItemQueryEntity.Languages> languages;

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
        @JsonProperty("slug")
        String slug;
    }

    public static Map<String, Set<Object>> entitiesToFilters(final List<ProjectPageItemFiltersQueryEntity> filtersViewEntities) {
        final Map<String, Set<Object>> filters = new HashMap<>();
        final Set<EcosystemView> ecosystems = new HashSet<>();
        final Set<LanguageView> languages = new HashSet<>();
        for (ProjectPageItemFiltersQueryEntity filtersViewEntity : filtersViewEntities) {
            if (nonNull(filtersViewEntity.ecosystems)) {
                filtersViewEntity.ecosystems.stream()
                        .filter(ecosystem -> nonNull(ecosystem.name))
                        .map(ecosystem -> EcosystemView.builder()
                                .id(ecosystem.id)
                                .url(ecosystem.url)
                                .logoUrl(ecosystem.logoUrl)
                                .slug(ecosystem.slug)
                                .name(ecosystem.name).build())
                        .forEach(ecosystems::add);
            }
            if (nonNull(filtersViewEntity.languages)) {
                filtersViewEntity.languages.stream()
                        .filter(Objects::nonNull)
                        .map(l -> LanguageView.builder()
                                .id(l.id)
                                .name(l.name)
                                .logoUrl(l.logoUrl)
                                .bannerUrl(l.bannerUrl)
                                .build())
                        .forEach(languages::add);
            }
        }
        filters.put(ProjectCardView.FilterBy.ECOSYSTEMS.name(),
                ecosystems.stream().map(Object.class::cast).collect(Collectors.toSet()));
        filters.put(ProjectCardView.FilterBy.LANGUAGES.name(),
                languages.stream().map(Object.class::cast).collect(Collectors.toSet()));
        return filters;
    }

}
