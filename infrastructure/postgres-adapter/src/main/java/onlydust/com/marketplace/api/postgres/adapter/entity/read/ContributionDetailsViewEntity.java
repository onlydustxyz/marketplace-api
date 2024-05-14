package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
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
@IdClass(ContributionDetailsViewEntity.PrimaryKey.class)
@Immutable
public class ContributionDetailsViewEntity {
    @Id
    String id;
    Date createdAt;
    Date completedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "contribution_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    Type type;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "contribution_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    Status status;

    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    Long githubAuthorId;
    String githubAuthorLogin;
    String githubAuthorAvatarUrl;
    Integer githubCommentsCount;
    Integer githubCommitsCount;
    Integer githubUserCommitsCount;

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

    String contributorLogin;
    String contributorAvatarUrl;
    Long contributorId;
    Boolean contributorIsRegistered;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ContributionLinkViewEntity> links;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "github_pull_request_review_state")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    ContributionViewEntity.GithubPullRequestReviewState prReviewState;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        String id;
        UUID projectId;
    }

    public ContributionDetailsView toView() {
        final var contributor = ContributorLinkView.builder()
                .login(contributorLogin)
                .avatarUrl(contributorAvatarUrl)
                .githubUserId(contributorId)
                .isRegistered(contributorIsRegistered)
                .build();

        final var project = Project.builder()
                .id(projectId)
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

        return ContributionDetailsView.builder()
                .id(id)
                .createdAt(createdAt)
                .completedAt(completedAt)
                .type(type.toView())
                .status(status.toView())
                .contributor(contributor)
                .githubNumber(githubNumber)
                .githubStatus(githubStatus)
                .githubTitle(githubTitle)
                .githubHtmlUrl(githubHtmlUrl)
                .githubBody(githubBody)
                .githubCommentsCount(githubCommentsCount)
                .githubUserCommitsCount(githubUserCommitsCount)
                .githubCommitsCount(githubCommitsCount)
                .githubAuthor(author)
                .project(project)
                .githubRepo(repo)
                .links(Optional.ofNullable(links).orElse(List.of()).stream().map(ContributionLinkViewEntity::toView).toList())
                .prReviewState(Optional.ofNullable(prReviewState).map(ContributionViewEntity.GithubPullRequestReviewState::toView).orElse(null))
                .build();
    }

    public enum Type {
        PULL_REQUEST, ISSUE, CODE_REVIEW;

        public ContributionType toView() {
            return switch (this) {
                case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                case ISSUE -> ContributionType.ISSUE;
                case CODE_REVIEW -> ContributionType.CODE_REVIEW;
            };
        }
    }

    public enum Status {
        IN_PROGRESS, COMPLETED, CANCELLED;

        public ContributionStatus toView() {
            return switch (this) {
                case IN_PROGRESS -> ContributionStatus.IN_PROGRESS;
                case COMPLETED -> ContributionStatus.COMPLETED;
                case CANCELLED -> ContributionStatus.CANCELLED;
            };
        }
    }
}
