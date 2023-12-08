package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@IdClass(ContributionDetailsViewEntity.PrimaryKey.class)
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "contribution_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "github_pull_request_review_state", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContributionDetailsViewEntity {
    @Id
    String id;
    Date createdAt;
    Date completedAt;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "contribution_type")
    Type type;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "contribution_status")
    Status status;

    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    Long githubAuthorId;
    String githubAuthorLogin;
    String githubAuthorHtmlUrl;
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
    @org.hibernate.annotations.Type(type = "project_visibility")
    ProjectVisibilityEnumEntity projectVisibility;

    Long repoId;
    String repoOwner;
    String repoName;
    String repoHtmlUrl;

    String contributorLogin;
    String contributorAvatarUrl;
    String contributorHtmlUrl;
    Long contributorId;
    Boolean contributorIsRegistered;

    @org.hibernate.annotations.Type(type = "jsonb")
    List<ContributionLinkViewEntity> links;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "github_pull_request_review_state")
    GithubPullRequestReviewState prReviewState;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        String id;
        UUID projectId;
    }

    public ContributionDetailsView toView() {
        final var contributor = ContributorLinkView.builder()
                .login(contributorLogin)
                .url(contributorHtmlUrl)
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
                .visibility(ProjectMapper.projectVisibilityToDomain(projectVisibility))
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
                .url(githubAuthorHtmlUrl)
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
                .prReviewState(Optional.ofNullable(prReviewState).map(GithubPullRequestReviewState::toView).orElse(null))
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
