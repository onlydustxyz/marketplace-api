package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.ScoredApplication;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class OnApplicationCreatedTrackingEvent extends Event {
    @NonNull
    Application.Id applicationId;
    @NonNull
    UUID projectId;
    @NonNull
    Long applicantGithubId;
    @NonNull
    Application.Origin origin;
    @NonNull
    ZonedDateTime appliedAt;
    @NonNull
    GithubIssue.Id issueId;
    Integer availabilityScore;
    Integer bestProjectsSimilarityScore;
    Integer mainRepoLanguageUserScore;
    Integer projectFidelityScore;
    Integer recommendationScore;

    public static Event of(OnApplicationCreated onApplicationCreated, ScoredApplication scoredApplication) {
        return OnApplicationCreatedTrackingEvent.builder()
                .applicationId(onApplicationCreated.applicationId())
                .projectId(onApplicationCreated.projectId())
                .applicantGithubId(onApplicationCreated.applicantId())
                .origin(onApplicationCreated.origin())
                .appliedAt(onApplicationCreated.appliedAt())
                .issueId(onApplicationCreated.issueId())
                .availabilityScore(scoredApplication.availabilityScore())
                .bestProjectsSimilarityScore(scoredApplication.bestProjectsSimilarityScore())
                .mainRepoLanguageUserScore(scoredApplication.mainRepoLanguageUserScore())
                .projectFidelityScore(scoredApplication.projectFidelityScore())
                .recommendationScore(scoredApplication.recommendationScore())
                .build();
    }
}