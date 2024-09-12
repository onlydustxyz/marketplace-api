package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.PullRequestReviewState;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@IdClass(ContributionViewEntity.PrimaryKey.class)
@Table(name = "contributions", schema = "indexer_exp")
@Immutable
public class ContributionViewEntity {
    @Id
    String id;

    Date createdAt;
    Date lastUpdatedAt;
    Date completedAt;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "contribution_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionType type;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "contribution_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionStatus status;
    Long contributorId;
    String contributorLogin;
    String contributorHtmlUrl;
    String contributorAvatarUrl;
    Boolean contributorIsRegistered;
    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    Long githubAuthorId;
    String githubAuthorLogin;
    String githubAuthorHtmlUrl;
    String githubAuthorAvatarUrl;

    @Id
    UUID projectId;
    String projectName;
    String projectKey;
    String projectShortDescription;
    String projectLogoUrl;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "project_visibility")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ProjectVisibility projectVisibility;

    Long repoId;
    String repoOwner;
    String repoName;
    String repoHtmlUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ContributionLinkJsonDto> links;

    @JdbcTypeCode(SqlTypes.JSON)
    List<UUID> rewardIds;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "github_pull_request_review_state")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    GithubPullRequestReviewState prReviewState;

    public ContributionView toView() {
        final var project = Project.builder()
                .id(ProjectId.of(projectId))
                .slug(projectKey)
                .name(projectName)
                .shortDescription(projectShortDescription)
                .logoUrl(projectLogoUrl)
                .visibility(projectVisibility)
                .build();

        final var repo = GithubRepo.builder()
                .id(repoId)
                .owner(repoOwner)
                .name(repoName)
                .htmlUrl(repoHtmlUrl)
                .build();

        final var author = ContributorLinkView.builder()
                .githubUserId(githubAuthorId)
                .login(githubAuthorLogin)
                .avatarUrl(githubAuthorAvatarUrl)
                .build();

        return ContributionView.builder()
                .id(id)
                .createdAt(createdAt)
                .lastUpdatedAt(lastUpdatedAt)
                .completedAt(completedAt)
                .type(type)
                .status(status)
                .contributor(ContributorLinkView.builder()
                        .githubUserId(contributorId)
                        .login(contributorLogin)
                        .avatarUrl(contributorAvatarUrl)
                        .isRegistered(contributorIsRegistered)
                        .build())
                .githubNumber(githubNumber)
                .githubStatus(githubStatus)
                .githubTitle(githubTitle)
                .githubHtmlUrl(githubHtmlUrl)
                .githubBody(githubBody)
                .githubAuthor(author)
                .project(project)
                .githubRepo(repo)
                .links(Optional.ofNullable(links).orElse(List.of()).stream().map(ContributionLinkJsonDto::toView).toList())
                .rewardIds(Optional.ofNullable(rewardIds).orElse(List.of()))
                .prReviewState(Optional.ofNullable(prReviewState).map(GithubPullRequestReviewState::toView).orElse(null))
                .build();
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        String id;
        UUID projectId;
    }

    public enum GithubPullRequestReviewState {
        PENDING_REVIEWER, UNDER_REVIEW, APPROVED, CHANGES_REQUESTED;

        public PullRequestReviewState toView() {
            return switch (this) {
                case UNDER_REVIEW -> PullRequestReviewState.UNDER_REVIEW;
                case APPROVED -> PullRequestReviewState.APPROVED;
                case CHANGES_REQUESTED -> PullRequestReviewState.CHANGES_REQUESTED;
                case PENDING_REVIEWER -> PullRequestReviewState.PENDING_REVIEWER;
            };
        }
    }
}
