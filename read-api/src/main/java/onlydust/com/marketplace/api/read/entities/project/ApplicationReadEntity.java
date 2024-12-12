package onlydust.com.marketplace.api.read.entities.project;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationOrigin;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationShortResponse;
import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;


@Entity
@NoArgsConstructor
@Table(name = "applications", schema = "public")
@Immutable
@Getter
@Accessors(fluent = true)
public class ApplicationReadEntity {
    @Id
    @NonNull
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
    String commentBody;

    ZonedDateTime ignoredAt;

    @NonNull
    @Enumerated(EnumType.STRING)
    ProjectApplicationOrigin origin;

    public ProjectApplicationShortResponse toShortResponse() {
        return new ProjectApplicationShortResponse()
                .id(id)
                .applicant(applicant.toContributorResponse())
                .githubComment(commentBody)
                .project(project.toLinkResponse())
                .issue(issue == null ? null : issue.toLinkDto())
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
                .githubComment(commentBody)
                ;
    }
}
