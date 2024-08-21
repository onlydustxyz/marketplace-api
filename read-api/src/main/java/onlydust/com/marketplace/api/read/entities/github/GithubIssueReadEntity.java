package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.URI;
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "indexer_exp.github_issue_status")
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "indexer_exp",
            name = "github_issues_labels",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @OrderBy("name")
    @NonNull
    List<GithubLabelReadEntity> labels;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "indexer_exp",
            name = "github_issues_assignees",
            joinColumns = @JoinColumn(name = "issueId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "githubUserId")
    )
    @NonNull
    List<AllUserReadEntity> assignees;

    @OneToMany(mappedBy = "issue", fetch = FetchType.LAZY)
    @NonNull
    Set<ApplicationReadEntity> applications;

    public GithubIssuePageItemResponse toPageItemResponse(@NonNull UUID projectId, Long githubUserId) {
        final var projectApplications = applications.stream()
                .filter(application -> application.projectId().equals(projectId))
                .toList();

        final var currentUserApplication = projectApplications.stream()
                .filter(application -> application.applicantId().equals(githubUserId))
                .findFirst();

        return new GithubIssuePageItemResponse()
                .id(id)
                .number(number)
                .title(title)
                .status(status)
                .htmlUrl(htmlUrl)
                .repo(repo.toShortResponse())
                .author(author.toContributorResponse())
                .createdAt(createdAt)
                .closedAt(closedAt)
                .body(body)
                .labels(labels.stream().map(GithubLabelReadEntity::toDto).toList())
                .applicants(projectApplications.stream().map(ApplicationReadEntity::applicant).map(AllUserReadEntity::toGithubUserResponse).toList())
                .assignees(assignees.stream().map(AllUserReadEntity::toGithubUserResponse).toList())
                .currentUserApplication(currentUserApplication.map(ApplicationReadEntity::toShortResponse).orElse(null))
                ;
    }

    public GithubIssueLinkResponse toLinkDto() {
        return new GithubIssueLinkResponse()
                .id(id)
                .number(number)
                .title(title)
                .status(status)
                .htmlUrl(htmlUrl)
                .repo(repo.toShortResponse())
                .author(author.toContributorResponse())
                ;
    }

    public GithubIssueResponse toDto(boolean asProjectLead) {
        final var response = new GithubIssueResponse()
                .id(id)
                .number(number)
                .title(title)
                .status(status)
                .htmlUrl(htmlUrl)
                .repo(repo.toShortResponse())
                .author(author.toContributorResponse())
                .createdAt(createdAt)
                .closedAt(closedAt)
                .body(body)
                .commentCount(commentsCount)
                .labels(labels.stream().map(GithubLabelReadEntity::toDto).toList())
                .applicants(applications.stream().map(ApplicationReadEntity::applicant).map(AllUserReadEntity::toGithubUserResponse).toList())
                .assignees(assignees.stream().map(AllUserReadEntity::toGithubUserResponse).toList())
                .languages(repo.languages().stream().distinct().map(LanguageReadEntity::toDto).toList());

        if (asProjectLead) {
            final var installationStatus = map(repo.owner().installation().map(GithubAppInstallationViewEntity::getStatus).orElse(null));
            response.setGithubAppInstallationStatus(installationStatus);
            if (installationStatus == GithubOrganizationInstallationStatus.MISSING_PERMISSIONS) {
                final var isAPersonalOrganization = switch (repo.owner().type()) {
                    case "USER":
                        yield true;
                    case "ORGANIZATION":
                        yield false;
                    default:
                        throw OnlyDustException.internalServerError("Invalid github organization type %s".formatted(repo.owner().type()));
                };
                if (isAPersonalOrganization) {
                    response.setGithubAppInstallationPermissionsUpdateUrl(
                            URI.create("https://github.com/settings/installations/%s"
                                    .formatted(repo.owner().installation().map(GithubAppInstallationViewEntity::getId).orElse(null))));
                } else {
                    response.setGithubAppInstallationPermissionsUpdateUrl(
                            URI.create("https://github.com/organizations/%s/settings/installations/%d/permissions/update"
                                    .formatted(repoOwnerLogin, repo.owner().installation().map(GithubAppInstallationViewEntity::getId).orElse(null))));
                }
            }
        }

        return response;
    }

    private GithubOrganizationInstallationStatus map(GithubAppInstallationViewEntity.Status status) {
        return status == null ? GithubOrganizationInstallationStatus.NOT_INSTALLED : switch (status) {
            case SUSPENDED -> GithubOrganizationInstallationStatus.SUSPENDED;
            case MISSING_PERMISSIONS -> GithubOrganizationInstallationStatus.MISSING_PERMISSIONS;
            case COMPLETE -> GithubOrganizationInstallationStatus.COMPLETE;
        };
    }
}
