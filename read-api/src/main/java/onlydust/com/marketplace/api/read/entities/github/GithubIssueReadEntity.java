package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.GithubIssueLinkResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.contract.model.GithubOrganizationInstallationStatus;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.bi.ContributorReadProjectionEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonReadEntity;
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
    @JoinColumn(name = "author_id", referencedColumnName = "githubUserId")
    @NonNull
    AllUserReadEntity author;

    @ManyToMany
    @JoinTable(
            schema = "public",
            name = "hackathon_issues",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "hackathon_id")
    )
    @NonNull
    Set<HackathonReadEntity> hackathons;

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
                .applicants(applications.stream().map(ApplicationReadEntity::applicant).map(ContributorReadProjectionEntity::toGithubUserResponse).toList())
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
