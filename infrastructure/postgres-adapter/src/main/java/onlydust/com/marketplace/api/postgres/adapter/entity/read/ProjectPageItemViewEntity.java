package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.domain.Sort;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Entity
@Table(name = "project_details", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectPageItemViewEntity {
    @Id
    @Column(name = "project_id")
    UUID projectId;
    Boolean hiring;
    @Column(name = "logo_url")
    String logoUrl;
    String key;
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Type(type = "project_visibility")
    @Enumerated(EnumType.STRING)
    ProjectVisibilityEnumEntity visibility;
    Integer rank;
    Integer repoCount;
    Integer contributorsCount;
    Integer projectLeadCount;
    Boolean isPendingProjectLead;
    @Type(type = "jsonb")
    List<Sponsor> sponsors;
    @Type(type = "jsonb")
    List<ProjectLead> projectLeads;
    @Type(type = "jsonb")
    List<Map<String, Long>> technologies;

    @EqualsAndHashCode
    public static class ProjectLead {

        @JsonProperty("id")
        UUID id;

        @JsonProperty("url")
        String url;
        @JsonProperty("avatarUrl")
        String avatarUrl;
        @JsonProperty("login")
        String login;
        @JsonProperty("githubId")
        Long githubId;
    }

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

    public ProjectCardView toView() {
        final ProjectCardView view = ProjectCardView.builder()
                .repoCount(this.repoCount)
                .id(this.projectId)
                .slug(this.key)
                .name(this.name)
                .shortDescription(this.shortDescription)
                .logoUrl(this.logoUrl)
                .hiring(this.hiring)
                .contributorCount(this.contributorsCount)
                .visibility(switch (this.visibility) {
                    case PUBLIC -> ProjectVisibility.PUBLIC;
                    case PRIVATE -> ProjectVisibility.PRIVATE;
                })
                .build();
        if (nonNull(this.technologies)) {
            this.technologies.forEach(view::addTechnologies);
        }
        if (nonNull(this.sponsors)) {
            this.sponsors.forEach(sponsor -> view.addSponsor(SponsorView.builder()
                    .id(sponsor.id)
                    .logoUrl(sponsor.logoUrl)
                    .name(sponsor.name)
                    .build()));
        }
        return view;
    }

    public static String getSponsorsJsonPath(List<String> sponsors) {
        if (isNull(sponsors) || sponsors.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", sponsors.stream().map(s -> "@.name == \"" + s + "\"").toList()) + ")";
    }

    public static String getTechnologiesJsonPath(List<String> technologies) {
        if (isNull(technologies) || technologies.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", technologies.stream().map(t -> "@.\"" + t + "\" > 0").toList()) + ")";
    }

    public static Sort getSort(ProjectCardView.SortBy sortBy) {
        sortBy = isNull(sortBy) ? ProjectCardView.SortBy.RANK : sortBy;
        return switch (sortBy) {
            case CONTRIBUTORS_COUNT -> Sort.by("contributors_count").descending().and(Sort.by("name").ascending());
            case REPOS_COUNT -> Sort.by("repo_count").descending().and(Sort.by("name").ascending());
            case RANK -> Sort.by("rank").ascending().and(Sort.by("name").ascending());
            case NAME -> Sort.by("name").ascending();
        };
    }
}
