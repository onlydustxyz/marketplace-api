package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationOrigin;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationShortResponse;
import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
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
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectLinkReadEntity project;
    UUID projectId;

    @NonNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "issueId", insertable = false, updatable = false)
    GithubIssueReadEntity issue;
    Long issueId;

    @NonNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "applicantId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserReadEntity applicant;
    Long applicantId;

    @NonNull
    String motivations;

    String problemSolvingApproach;

    @NonNull
    @Enumerated(EnumType.STRING)
    ProjectApplicationOrigin origin;

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
                .project(project.toLinkResponse())
                .issue(issue.toLinkDto())
                .applicant(applicant.toRankedContributorResponse())
                .receivedAt(receivedAt)
                ;
    }

    public ProjectApplicationResponse toDto() {
        return new ProjectApplicationResponse()
                .id(id)
                .projectId(projectId)
                .issue(issue.toLinkDto())
                .applicant(applicant.toContributorResponse())
                .origin(origin)
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach)
                ;
    }
}
