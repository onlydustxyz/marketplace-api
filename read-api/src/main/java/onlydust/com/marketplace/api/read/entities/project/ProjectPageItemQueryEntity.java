package onlydust.com.marketplace.api.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Entity
public class ProjectPageItemQueryEntity {
    @Id
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String logoUrl;
    Boolean hiring;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;
    Integer repoCount;
    Integer contributorCount;
    @JdbcTypeCode(SqlTypes.JSON)
    List<RegisteredUserResponse> projectLeads;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<ProjectTag> tags;
    BigDecimal remainingUsdBudget;
    Boolean hasGoodFirstIssues;
    Boolean hasReposWithoutGithubAppInstalled;
    Boolean isInvitedAsProjectLead;

    public List<RegisteredUserResponse> projectLeads() {
        return projectLeads == null ? List.of() : projectLeads.stream().sorted(comparing(RegisteredUserResponse::getLogin)).toList();
    }

    public List<ProjectCategoryResponse> categories() {
        return categories == null ? List.of() : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList();
    }

    public List<EcosystemLinkResponse> ecosystems() {
        return ecosystems == null ? List.of() : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList();
    }

    public List<LanguageResponse> languages() {
        return languages == null ? List.of() : languages.stream().sorted(comparing(LanguageResponse::getName)).toList();
    }

    public List<ProjectTag> tags() {
        return tags == null ? List.of() : tags.stream().sorted(comparing(ProjectTag::name)).toList();
    }

    public ProjectPageItemResponse toDto(UUID userId) {
        final var isProjectLead = nonNull(userId) && nonNull(this.projectLeads) && this.projectLeads.stream().anyMatch(lead -> lead.getId().equals(userId));

        return new ProjectPageItemResponse()
                .id(this.id)
                .slug(this.slug)
                .name(this.name)
                .shortDescription(this.shortDescription)
                .logoUrl(this.logoUrl)
                .hiring(this.hiring)
                .visibility(this.visibility)
                .contributorCount(this.contributorCount)
                .remainingUsdBudget(isProjectLead ? remainingUsdBudget : null)
                .repoCount(this.repoCount)
                .tags(this.tags())
                .ecosystems(this.ecosystems())
                .leaders(this.projectLeads())
                .languages(this.languages())
                .isInvitedAsProjectLead(this.isInvitedAsProjectLead)
                .hasMissingGithubAppInstallation(isProjectLead ? hasReposWithoutGithubAppInstalled : null);
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class Ecosystem {
        @EqualsAndHashCode.Include
        @JsonProperty("id")
        UUID id;
        @JsonProperty("url")
        String url;
        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("name")
        String name;
        @JsonProperty("slug")
        String slug;
        @JsonProperty("hidden")
        Boolean hidden;

        public EcosystemLinkResponse toDto() {
            return new EcosystemLinkResponse()
                    .id(id)
                    .name(name)
                    .slug(slug)
                    .logoUrl(logoUrl)
                    .url(url)
                    .hidden(hidden);
        }
    }
}
