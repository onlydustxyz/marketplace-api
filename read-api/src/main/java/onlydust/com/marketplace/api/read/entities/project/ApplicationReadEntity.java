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
    @ManyToOne(optional = false)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectReadEntity project;
    UUID projectId;

    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "issueId", insertable = false, updatable = false)
    GithubIssueReadEntity issue;
    Long issueId;

    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "applicantId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserReadEntity applicant;
    Long applicantId;

    @NonNull
    String motivations;

    String problemSolvingApproach;

    @NonNull
    @Enumerated(EnumType.STRING)
    ProjectApplicationOrigin origin;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
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
                .projectId(projectId)
                .issue(issue.toLinkDto())
                .applicant(applicant.toContributorResponse())
                .recommandationScore(ranking.recommendationScore())
                ;
    }

    public ProjectApplicationResponse toDto() {
        return new ProjectApplicationResponse()
                .id(id)
                .projectId(projectId)
                .issue(issue.toLinkDto())
                .applicant(applicant.toContributorResponse())
                .origin(origin)
                .recommendationScore(ranking.recommendationScore())
                .availabilityScore(ranking.availabilityScore())
                .languageScore(ranking.mainRepoLanguageUserScore())
                .fidelityScore(ranking.projectFidelityScore())
                .appliedDistinctProjectCount(ranking.appliedProjectCount())
                .pendingApplicationCountOnThisProject(ranking.pendingApplicationCountOnThisProject())
                .pendingApplicationCountOnOtherProjects(ranking.pendingApplicationCountOnOtherProjects())
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach)
                ;
    }
}
