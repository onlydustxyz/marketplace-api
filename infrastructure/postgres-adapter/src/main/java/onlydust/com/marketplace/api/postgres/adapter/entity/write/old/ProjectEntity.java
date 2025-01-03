package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategorySuggestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectEcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.backoffice.ProjectView;
import org.hibernate.annotations.*;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "projects", schema = "public")
@DynamicUpdate
@SelectBeforeUpdate
public class ProjectEntity {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Column(name = "telegram_link")
    String telegramLink;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "hiring")
    Boolean hiring;
    @Column(name = "rank", updatable = false)
    Integer rank;
    @Column(name = "slug")
    @EqualsAndHashCode.Exclude
    String slug;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "visibility", updatable = false)
    ProjectVisibility visibility;
    @Column(name = "reward_ignore_pull_requests_by_default")
    Boolean ignorePullRequests;
    @Column(name = "reward_ignore_issues_by_default")
    Boolean ignoreIssues;
    @Column(name = "reward_ignore_code_reviews_by_default")
    Boolean ignoreCodeReviews;
    @Column(name = "reward_ignore_contributions_before_date_by_default")
    Date ignoreContributionsBefore;
    boolean botNotifyExternalApplications;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectLeaderInvitationEntity> projectLeaderInvitations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectLeadEntity> projectLeaders;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectRepoEntity> repos;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectMoreInfoEntity> moreInfos;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectEcosystemEntity> ecosystems;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    Set<ProjectProjectCategoryEntity> categories;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    @NonNull
    Set<ProjectCategorySuggestionEntity> categorySuggestions;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "projectId")
    @NonNull
    Set<ProjectContributorLabelEntity> contributorLabels;

    public Project toDomain() {
        return Project.builder()
                .id(ProjectId.of(id))
                .slug(slug)
                .name(name)
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .logoUrl(logoUrl)
                .moreInfoUrl(isNull(moreInfos) ? null : moreInfos.stream().findFirst().map(ProjectMoreInfoEntity::getUrl).orElse(null))
                .hiring(hiring)
                .visibility(visibility)
                .botNotifyExternalApplications(botNotifyExternalApplications)
                .build();
    }

    public ProjectView toBoView() {
        return ProjectView.builder()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .build();
    }

    public ProjectShortView toView() {
        return ProjectShortView.builder()
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .shortDescription(shortDescription)
                .id(ProjectId.of(id))
                .build();
    }
}
