package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.*;

@Entity
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "contribution_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(
        typeClass = EnumArrayType.class,
        defaultForType = CodeReviewState[].class,
        parameters = {
                @org.hibernate.annotations.Parameter(
                        name = AbstractArrayType.SQL_ARRAY_TYPE,
                        value = "github_code_review_state"
                )
        }
)
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
    Long contributorId;

    @org.hibernate.annotations.Type(type = "jsonb")
    List<ContributionLinkViewEntity> links;

    @Column(columnDefinition = "github_code_review_state[]")
    CodeReviewState[] codeReviewStates;

    public ContributionDetailsView toView() {
        final var contributor = GithubUserIdentity.builder()
                .githubLogin(contributorLogin)
                .githubAvatarUrl(contributorAvatarUrl)
                .githubUserId(contributorId)
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
                .githubAuthor(author)
                .project(project)
                .githubRepo(repo)
                .links(Optional.ofNullable(links).orElse(List.of()).stream().map(ContributionLinkViewEntity::toView).toList())
                .codeReviewStates(codeReviewStates == null ? null : Arrays.stream(codeReviewStates).map(CodeReviewState::toDomain).toList())
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
