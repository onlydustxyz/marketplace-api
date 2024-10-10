package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "applications", schema = "public")
@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class ApplicationEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    ZonedDateTime receivedAt;
    @NonNull
    UUID projectId;
    @NonNull
    Long applicantId;
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    Application.Origin origin;
    @NonNull
    Long issueId;

    @NonNull
    Long commentId;
    @NonNull
    String motivations;
    String problemSolvingApproach;

    ZonedDateTime ignoredAt;

    public ApplicationEntity(@NonNull UUID id, @NonNull ZonedDateTime receivedAt, @NonNull UUID projectId, @NonNull Long applicantId,
                             @NonNull Application.Origin origin, @NonNull Long issueId, @NonNull Long commentId,
                             @NonNull String motivations, String problemSolvingApproach) {
        this.id = id;
        this.receivedAt = receivedAt;
        this.projectId = projectId;
        this.applicantId = applicantId;
        this.origin = origin;
        this.issueId = issueId;
        this.commentId = commentId;
        this.motivations = motivations;
        this.problemSolvingApproach = problemSolvingApproach;
    }

    public static ApplicationEntity fromDomain(Application application) {
        return ApplicationEntity.builder()
                .id(application.id().value())
                .receivedAt(application.appliedAt())
                .projectId(application.projectId().value())
                .applicantId(application.applicantId())
                .origin(application.origin())
                .issueId(application.issueId().value())
                .commentId(application.commentId().value())
                .motivations(application.motivations())
                .problemSolvingApproach(application.problemSolvingApproach())
                .ignoredAt(application.ignoredAt())
                .build();
    }

    public Application toDomain() {
        return new Application(
                Application.Id.of(id),
                ProjectId.of(projectId),
                applicantId,
                origin,
                receivedAt,
                GithubIssue.Id.of(issueId),
                GithubComment.Id.of(commentId),
                motivations,
                problemSolvingApproach,
                ignoredAt);
    }
}
