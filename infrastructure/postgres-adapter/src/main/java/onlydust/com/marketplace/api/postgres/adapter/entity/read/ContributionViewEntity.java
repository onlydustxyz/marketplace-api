package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.ContributionView;
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
@IdClass(ContributionViewEntity.PrimaryKey.class)
@Table(name = "contributions", schema = "indexer_exp")
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "contribution_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "github_pull_request_review_state", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ContributionViewEntity {
    @Id
    String id;

    Date createdAt;
    Date lastUpdatedAt;
    Date completedAt;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "contribution_type")
    Type type;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "contribution_status")
    Status status;
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
    @org.hibernate.annotations.Type(type = "project_visibility")
    ProjectVisibilityEnumEntity projectVisibility;

    Long repoId;
    String repoOwner;
    String repoName;
    String repoHtmlUrl;

    @org.hibernate.annotations.Type(type = "jsonb")
    List<ContributionLinkViewEntity> links;

    @org.hibernate.annotations.Type(type = "jsonb")
    List<UUID> rewardIds;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.Type(type = "github_pull_request_review_state")
    GithubPullRequestReviewState prReviewState;

    public ContributionView toView() {
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

        return ContributionView.builder()
                .id(id)
                .createdAt(createdAt)
                .lastUpdatedAt(lastUpdatedAt)
                .completedAt(completedAt)
                .type(type.toView())
                .status(status.toView())
                .contributor(ContributorLinkView.builder()
                        .githubUserId(contributorId)
                        .login(contributorLogin)
                        .url(contributorHtmlUrl)
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
                .links(Optional.ofNullable(links).orElse(List.of()).stream().map(ContributionLinkViewEntity::toView).toList())
                .rewardIds(Optional.ofNullable(rewardIds).orElse(List.of()))
                .prReviewState(Optional.ofNullable(prReviewState).map(GithubPullRequestReviewState::toView).orElse(null))
                .build();
    }

    public enum Type {
        PULL_REQUEST, ISSUE, CODE_REVIEW;

        public static Type fromView(ContributionType contributionType) {
            return contributionType == null ? null : switch (contributionType) {
                case PULL_REQUEST -> Type.PULL_REQUEST;
                case ISSUE -> Type.ISSUE;
                case CODE_REVIEW -> Type.CODE_REVIEW;
            };
        }

        public static String fromViewToString(ContributionType contributionType) {
            return Type.fromView(contributionType) != null ? Type.fromView(contributionType).name() : null;
        }

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

        public static Status fromView(ContributionStatus contributionStatus) {
            return contributionStatus == null ? null : switch (contributionStatus) {
                case IN_PROGRESS -> Status.IN_PROGRESS;
                case COMPLETED -> Status.COMPLETED;
                case CANCELLED -> Status.CANCELLED;
            };
        }

        public static String fromViewToString(ContributionStatus contributionStatus) {
            return Status.fromView(contributionStatus) != null ? Status.fromView(contributionStatus).name() : null;
        }

        public ContributionStatus toView() {
            return switch (this) {
                case IN_PROGRESS -> ContributionStatus.IN_PROGRESS;
                case COMPLETED -> ContributionStatus.COMPLETED;
                case CANCELLED -> ContributionStatus.CANCELLED;
            };
        }
    }

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        String id;
        UUID projectId;
    }
}
