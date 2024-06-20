package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationShortResponse;
import onlydust.com.marketplace.bff.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.project.domain.model.Application;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "applications", schema = "public")
@Immutable
@Accessors(fluent = true)
public class ApplicationReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID id;

    @NonNull
    ZonedDateTime receivedAt;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "projectId")
    ProjectReadEntity project;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "issueId")
    GithubIssueReadEntity issue;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "applicantId", referencedColumnName = "githubUserId")
    AllUserReadEntity applicant;

    @NonNull
    String motivations;

    String problemSolvingApproach;

    @NonNull
    @Enumerated(EnumType.STRING)
    Application.Origin origin;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "applicationId")
    ApplicationRankingReadEntity ranking;

    public ProjectApplicationShortResponse toShortResponse() {
        return new ProjectApplicationShortResponse()
                .id(id)
                .applicant(applicant.toContributorResponse())
                .motivations(motivations)
                .problemSolvingApproach(problemSolvingApproach)
                .project(project.toLinkResponse())
                ;
    }

    public ProjectApplicationPageItemResponse toPageItemDto() {
        return new ProjectApplicationPageItemResponse()
                .id(id)
                .projectId(project.getId())
                .issue(issue.toLinkDto())
                .applicant(applicant.toContributorResponse())
                .recommandationScore(ranking.recommandationScore())
                ;
    }
}
