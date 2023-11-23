package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.SponsorView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "project_details", schema = "public")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ProjectPageItemFiltersViewEntity {
    @Id
    @Column(name = "project_id")
    UUID projectId;
    @Type(type = "jsonb")
    List<Sponsor> sponsors;
    @Type(type = "jsonb")
    List<Map<String, Long>> technologies;

    @EqualsAndHashCode
    public static class Sponsor {

        @JsonProperty("url")
        String url;

        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
    }

    public static Map<String, Set<Object>> entitiesToFilters(final List<ProjectPageItemFiltersViewEntity> filtersViewEntities) {
        final Map<String, Set<Object>> filters = new HashMap<>();
        final Set<String> technologyNames = new HashSet<>();
        final Set<SponsorView> sponsors = new HashSet<>();
        for (ProjectPageItemFiltersViewEntity filtersViewEntity : filtersViewEntities) {
            if (nonNull(filtersViewEntity.technologies)) {
                filtersViewEntity.technologies.stream()
                        .filter(Objects::nonNull)
                        .map(Map::keySet)
                        .forEach(technologyNames::addAll);
            }
            if (nonNull(filtersViewEntity.sponsors)) {
                filtersViewEntity.sponsors.stream()
                        .filter(sponsor -> nonNull(sponsor.name))
                        .map(sponsor -> SponsorView.builder()
                                .id(sponsor.id)
                                .url(sponsor.url)
                                .logoUrl(sponsor.logoUrl)
                                .name(sponsor.name).build())
                        .forEach(sponsors::add);
            }
        }
        filters.put(ProjectCardView.FilterBy.TECHNOLOGIES.name(),
                technologyNames.stream().map(Object.class::cast).collect(Collectors.toSet()));
        filters.put(ProjectCardView.FilterBy.SPONSORS.name(),
                sponsors.stream().map(Object.class::cast).collect(Collectors.toSet()));
        return filters;
    }

}
