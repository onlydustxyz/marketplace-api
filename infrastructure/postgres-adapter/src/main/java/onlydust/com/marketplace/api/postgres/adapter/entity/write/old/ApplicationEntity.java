package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "applications", schema = "public")
@Accessors(fluent = true, chain = true)
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
    UUID applicantId;
    @NonNull
    Long issueId;
    @NonNull
    Long commentId;
    @NonNull
    String motivations;
    String problemSolvingApproach;

    public static ApplicationEntity fromDomain(Application application) {
        return ApplicationEntity.builder()
                .id(application.id().value())
                .receivedAt(application.appliedAt())
                .projectId(application.projectId())
                .applicantId(application.applicantId())
                .issueId(application.issueId().value())
                .commentId(application.commentId().value())
                .motivations(application.motivations())
                .problemSolvingApproach(application.problemSolvingApproach())
                .build();
    }

    public Application toDomain() {
        return new Application(
                Application.Id.of(id),
                projectId,
                applicantId,
                receivedAt,
                GithubIssue.Id.of(issueId),
                GithubComment.Id.of(commentId),
                motivations,
                problemSolvingApproach);
    }
}
