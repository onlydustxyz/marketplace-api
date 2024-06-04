package onlydust.com.marketplace.bff.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemResponse;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@NoArgsConstructor
@Data
@Immutable
public class ProjectPageItemFiltersQueryEntity {
    @Id
    UUID id;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectPageItemQueryEntity.Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectPageItemQueryEntity.Languages> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Category> categories;

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class Category {
        @EqualsAndHashCode.Include
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
        @JsonProperty("iconSlug")
        String iconSlug;
    }


    public static Set<LanguageResponse> languagesOf(final List<ProjectPageItemFiltersQueryEntity> entities) {
        final Set<LanguageResponse> languages = new HashSet<>();
        for (var entity : entities) {
            if (nonNull(entity.languages)) {
                languages.addAll(entity.languages.stream()
                        .filter(l -> nonNull(l.name))
                        .map(l -> new LanguageResponse()
                                .id(l.id())
                                .name(l.name())
                                .logoUrl(l.logoUrl())
                                .bannerUrl(l.bannerUrl())
                        ).toList());
            }
        }
        return languages;
    }

    public static Set<EcosystemResponse> ecosystemsOf(final List<ProjectPageItemFiltersQueryEntity> entities) {
        final Set<EcosystemResponse> ecosystems = new HashSet<>();
        for (var entity : entities) {
            if (nonNull(entity.ecosystems)) {
                ecosystems.addAll(entity.ecosystems.stream()
                        .filter(e -> nonNull(e.name))
                        .map(e -> new EcosystemResponse()
                                .id(e.id())
                                .name(e.name())
                                .slug(e.slug())
                                .logoUrl(e.logoUrl())
                                .url(e.url())
                        ).toList());
            }
        }
        return ecosystems;
    }

    public static Set<ProjectCategoryResponse> categoriesOf(final List<ProjectPageItemFiltersQueryEntity> entities) {
        final Set<ProjectCategoryResponse> categories = new HashSet<>();
        for (var entity : entities) {
            if (nonNull(entity.categories)) {
                categories.addAll(entity.categories.stream()
                        .filter(c -> nonNull(c.id()))
                        .map(c -> new ProjectCategoryResponse()
                                .id(c.id())
                                .name(c.name())
                                .iconSlug(c.iconSlug())
                        ).toList());
            }
        }
        return categories;
    }

}
