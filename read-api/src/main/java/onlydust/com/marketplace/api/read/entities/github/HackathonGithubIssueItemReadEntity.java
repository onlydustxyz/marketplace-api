package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.isNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(schema = "indexer_exp", name = "github_issues")
@Immutable
@Accessors(fluent = true)
public class HackathonGithubIssueItemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    Long id;

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
    UserLinkResponse author;

    @JdbcTypeCode(SqlTypes.JSON)
    List<String> labels;

    @JdbcTypeCode(SqlTypes.JSON)
    List<UserLinkResponse> assignees;

    @JdbcTypeCode(SqlTypes.JSON)
    List<UserLinkResponse> applicants;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> projects;
    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    GithubRepoLinkResponse repo;


    public IssuePageItem toPageItem() {
        return new IssuePageItem()
                .id(id)
                .number(number)
                .status(switch (status) {
                    case OPEN -> onlydust.com.backoffice.api.contract.model.GithubIssueStatus.OPEN;
                    case COMPLETED -> onlydust.com.backoffice.api.contract.model.GithubIssueStatus.COMPLETED;
                    case CANCELLED -> onlydust.com.backoffice.api.contract.model.GithubIssueStatus.CANCELLED;
                })
                .title(title)
                .repo(repo)
                .projects(isNull(projects) ? List.of() : projects.stream().sorted(Comparator.comparing(ProjectLinkResponse::getName)).toList())
                .author(author)
                .labels(isNull(labels) ? List.of() : labels.stream().sorted(String::compareTo).toList())
                .assignees(isNull(assignees) ? List.of() : assignees.stream().sorted(Comparator.comparing(UserLinkResponse::getLogin)).toList())
                .applicants(isNull(applicants) ? List.of() : applicants.stream().sorted(Comparator.comparing(UserLinkResponse::getLogin)).toList());
    }
}
