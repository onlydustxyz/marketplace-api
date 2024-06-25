package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.GithubIssue;
import onlydust.com.marketplace.api.contract.model.GithubIssueLinkResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.contract.model.ProjectIssuesPageItemResponse;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_issues")
@Immutable
@Accessors(fluent = true)
public class GithubIssueReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    GithubRepoReadEntity repo;

    @NonNull
    Long number;
    @NonNull
    String title;

    @Enumerated(EnumType.STRING)
    @NonNull
    GithubIssueStatus status;

    @NonNull
    ZonedDateTime createdAt;
    ZonedDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    GithubAccountReadEntity author;

    @NonNull
    String htmlUrl;
    String body;
    @NonNull
    Integer commentsCount;
    @NonNull
    String repoOwnerLogin;
    @NonNull
    String repoName;
    @NonNull
    String repoHtmlUrl;
    @NonNull
    String authorLogin;
    @NonNull
    String authorHtmlUrl;
    @NonNull
    String authorAvatarUrl;

    @ManyToMany
    @JoinTable(
            schema = "indexer_exp",
            name = "github_issues_labels",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @OrderBy("name")
    @NonNull
    List<GithubLabelReadEntity> labels;

    @ManyToMany
    @JoinTable(
            schema = "indexer_exp",
            name = "github_issues_assignees",
            joinColumns = @JoinColumn(name = "issueId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "githubUserId")
    )
    @NonNull
    List<AllUserReadEntity> assignees;

    @ManyToMany
    @JoinTable(
            schema = "public",
            name = "projects_good_first_issues",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    Set<ProjectReadEntity> goodFirstIssueOf;

    @OneToMany(mappedBy = "issue", fetch = FetchType.LAZY)
    @NonNull
    Set<ApplicationReadEntity> applications;

    public GithubIssue toDto(@NonNull UUID projectId, Long githubUserId) {
        final var projectApplications = applications.stream()
                .filter(application -> application.projectId().equals(projectId))
                .toList();

        final var currentUserApplication = projectApplications.stream()
                .filter(application -> application.applicant().githubUserId().equals(githubUserId))
                .findFirst();

        return new GithubIssue()
                .id(id)
                .number(number)
                .repository(repo.toShortResponse())
                .createdAt(createdAt)
                .closedAt(closedAt)
                .title(title)
                .body(body)
                .htmlUrl(htmlUrl)
                .status(status)
                .author(author.toContributorResponse())
                .commentCount(commentsCount)
                .labels(labels.stream().map(GithubLabelReadEntity::toDto).toList())
                .applicants(projectApplications.stream().map(ApplicationReadEntity::applicant).map(AllUserReadEntity::toGithubUserResponse).toList())
                .currentUserApplication(currentUserApplication.map(ApplicationReadEntity::toShortResponse).orElse(null))
                .languages(repo.languages().stream().distinct().map(LanguageReadEntity::toDto).toList())
                ;
    }

    public GithubIssueLinkResponse toLinkDto() {
        return new GithubIssueLinkResponse()
                .id(id)
                .number(number)
                .title(title)
                .status(status)
                .htmlUrl(htmlUrl);
    }

    public ProjectIssuesPageItemResponse toProjectIssueDto(@NonNull UUID projectId) {
        final var projectApplications = applications.stream()
                .filter(application -> application.projectId().equals(projectId))
                .toList();

        return new ProjectIssuesPageItemResponse()
                .id(id)
                .number(number)
                .repository(repo.toShortResponse())
                .createdAt(createdAt)
                .closedAt(closedAt)
                .title(title)
                .htmlUrl(htmlUrl)
                .status(status)
                .author(author.toContributorResponse())
                .applicants(projectApplications.stream().map(ApplicationReadEntity::applicant).map(AllUserReadEntity::toGithubUserResponse).toList())
                .assignees(assignees.stream().map(AllUserReadEntity::toGithubUserResponse).toList())
                ;
    }
}
