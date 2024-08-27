package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_issues")
@Immutable
@Accessors(fluent = true)
public class ProjectGithubIssueItemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ShortGithubRepoResponse repo;

    @NonNull
    Long number;
    @NonNull
    String title;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "indexer_exp.github_issue_status")
    @NonNull
    GithubIssueStatus status;

    @NonNull
    ZonedDateTime createdAt;
    ZonedDateTime closedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorResponse author;

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

    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubLabel> labels;

    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubUserResponse> assignees;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectApplicationShortResponse> applications;

    UUID projectId;
    String projectSlug;
    String projectName;
    String projectLogoUrl;

    public GithubIssuePageItemResponse toPageItemResponse(Long githubUserId) {

        final Optional<ProjectApplicationShortResponse> currentUserApplication = isNull(applications) ?
                Optional.empty() : applications.stream()
                .filter(application -> application.getApplicant().getGithubUserId().equals(githubUserId))
                .peek(projectApplicationShortResponse -> projectApplicationShortResponse.setProject(new ProjectLinkResponse()
                        .id(projectId)
                        .logoUrl(projectLogoUrl)
                        .slug(projectSlug)
                        .name(projectName)))
                .findFirst();

        return new GithubIssuePageItemResponse()
                .id(id)
                .number(number)
                .title(title)
                .status(status)
                .htmlUrl(htmlUrl)
                .repo(repo)
                .author(author)
                .createdAt(createdAt)
                .closedAt(closedAt)
                .body(body)
                .labels(isNull(labels) ? List.of() : labels.stream().sorted(Comparator.comparing(GithubLabel::getName)).toList())
                .applicants(isNull(applications) ? List.of() :
                        applications.stream().map(ProjectApplicationShortResponse::getApplicant)
                                .map(contributorResponse -> new GithubUserResponse(
                                        contributorResponse.getGithubUserId(),
                                        contributorResponse.getLogin(),
                                        contributorResponse.getAvatarUrl()))
                                .sorted(Comparator.comparing(GithubUserResponse::getLogin))
                                .toList())
                .assignees(isNull(assignees) ? List.of() : assignees.stream().sorted(Comparator.comparing(GithubUserResponse::getLogin)).toList())
                .currentUserApplication(currentUserApplication.orElse(null))
                ;
    }
}
