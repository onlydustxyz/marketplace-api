package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.EcosystemEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectMoreInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "project_details", schema = "public")
@Immutable
public class ProjectViewEntity {
    @Id
    @Column(name = "project_id", nullable = false)
    UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "created_at")
    Instant createdAt;
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
    @Column(name = "rank")
    Integer rank;
    @Column(name = "key", insertable = false)
    String key;
    @Enumerated(EnumType.STRING)
    @Type(PostgreSQLEnumType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibilityEnumEntity visibility;
    @Column(name = "reward_ignore_pull_requests_by_default")
    Boolean ignorePullRequests;
    @Column(name = "reward_ignore_issues_by_default")
    Boolean ignoreIssues;
    @Column(name = "reward_ignore_code_reviews_by_default")
    Boolean ignoreCodeReviews;
    @Column(name = "reward_ignore_contributions_before_date_by_default")
    Date ignoreContributionsBefore;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "projects_ecosystems",
            schema = "public",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "ecosystem_id")
    )
    List<EcosystemEntity> ecosystems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    @Builder.Default
    Set<ProjectSponsorEntity> sponsors = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_github_repos",
            schema = "public",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "github_repo_id")
    )
    List<GithubRepoEntity> repos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "projectId")
    Set<ProjectMoreInfoEntity> moreInfos;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "id.projectId")
    Set<ProjectTagEntity> tags;
}
