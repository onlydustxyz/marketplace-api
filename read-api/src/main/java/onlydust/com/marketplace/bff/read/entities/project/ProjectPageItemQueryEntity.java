package onlydust.com.marketplace.bff.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Entity
@Immutable
public class ProjectPageItemQueryEntity {
    @Id
    @Column(name = "id")
    UUID projectId;
    Boolean hiring;
    @Column(name = "logo_url")
    String logoUrl;
    String slug;
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;
    Integer repoCount;
    Integer contributorsCount;
    Boolean isPendingProjectLead;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLead> projectLeads;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Tag> tags;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageReadEntity> languages;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    ProjectLeadInfoQueryEntity projectLeadInfo;

    public static String getEcosystemsJsonPath(List<String> ecosystemSlugs) {
        if (isNull(ecosystemSlugs) || ecosystemSlugs.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", ecosystemSlugs.stream().map(s -> "@.slug == \"" + s + "\"").toList()) + ")";
    }

    public static String getTagsJsonPath(List<String> tags) {
        if (isNull(tags) || tags.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", tags.stream().map(s -> "@.name == \"" + s + "\"").toList()) + ")";
    }

    public static String getLanguagesJsonPath(List<String> languageSlugs) {
        if (isNull(languageSlugs) || languageSlugs.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", languageSlugs.stream().map(s -> "@.slug == \"" + s + "\"").toList()) + ")";
    }

    public ProjectPageItemResponse toDto(UUID userId) {
        final var isProjectLead = nonNull(userId) && nonNull(this.projectLeads) && this.projectLeads.stream().anyMatch(lead -> lead.id().equals(userId));

        return new ProjectPageItemResponse()
                .id(this.projectId)
                .slug(this.slug)
                .name(this.name)
                .shortDescription(this.shortDescription)
                .logoUrl(this.logoUrl)
                .hiring(this.hiring)
                .visibility(this.visibility)
                .contributorCount(this.contributorsCount)
                .remainingUsdBudget(isProjectLead ? projectLeadInfo.getRemainingUsdBudget() : null)
                .repoCount(this.repoCount)
                .tags(isNull(this.tags) ? List.of() : this.tags.stream().map(Tag::name).map(ProjectTag::valueOf).toList())
                .ecosystems(isNull(this.ecosystems) ? List.of() : this.ecosystems.stream().map(ecosystem -> new EcosystemResponse()
                        .id(ecosystem.id)
                        .logoUrl(ecosystem.logoUrl)
                        .name(ecosystem.name)
                        .slug(ecosystem.slug)
                        .url(ecosystem.url)
                ).toList())
                .leaders(isNull(this.projectLeads) ? List.of() : this.projectLeads.stream().map(projectLead -> new RegisteredUserResponse()
                        .id(projectLead.id)
                        .githubUserId(projectLead.githubId)
                        .avatarUrl(projectLead.avatarUrl)
                        .login(projectLead.login)
                ).toList())
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList())
                .isInvitedAsProjectLead(this.isPendingProjectLead)
                .hasMissingGithubAppInstallation(isProjectLead ? projectLeadInfo.getIsMissingGithubAppInstallation() : null);
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class ProjectLead {
        @EqualsAndHashCode.Include
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

        public EcosystemResponse toDto() {
            return new EcosystemResponse()
                    .id(id)
                    .name(name)
                    .slug(slug)
                    .logoUrl(logoUrl)
                    .url(url);
        }
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class Tag {
        @EqualsAndHashCode.Include
        @JsonProperty("name")
        String name;
    }
}
