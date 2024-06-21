package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.project.domain.model.ScoredApplication;
import onlydust.com.marketplace.project.domain.model.User;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE)
public class OnGithubIssueAssignedTrackingEvent extends Event {
    @NonNull
    Long issueId;
    @NonNull
    Long assigneeGithubId;
    @NonNull
    UUID assigneeUserId;
    @NonNull
    ZonedDateTime createdAt;
    @NonNull
    ZonedDateTime assignedAt;
    boolean isGoodFirstIssue;
    Integer availabilityScore;
    Integer bestProjectsSimilarityScore;
    Integer mainRepoLanguageUserScore;
    Integer projectFidelityScore;
    Integer recommendationScore;

    public static OnGithubIssueAssignedTrackingEvent of(OnGithubIssueAssigned onGithubIssueAssigned, User user, ScoredApplication scoredApplication) {
        return OnGithubIssueAssignedTrackingEvent.builder()
                .issueId(onGithubIssueAssigned.id())
                .assigneeGithubId(onGithubIssueAssigned.assigneeId())
                .assigneeUserId(user.getId())
                .createdAt(onGithubIssueAssigned.createdAt())
                .assignedAt(onGithubIssueAssigned.assignedAt())
                .isGoodFirstIssue(onGithubIssueAssigned.labels().stream().anyMatch(OnGithubIssueAssignedTrackingEvent::isGoodFirstIssue))
                .availabilityScore(scoredApplication == null ? null : scoredApplication.availabilityScore())
                .bestProjectsSimilarityScore(scoredApplication == null ? null : scoredApplication.bestProjectsSimilarityScore())
                .mainRepoLanguageUserScore(scoredApplication == null ? null : scoredApplication.mainRepoLanguageUserScore())
                .projectFidelityScore(scoredApplication == null ? null : scoredApplication.projectFidelityScore())
                .recommendationScore(scoredApplication == null ? null : scoredApplication.recommendationScore())
                .build();
    }

    private static boolean isGoodFirstIssue(String label) {
        return label.matches("(?i)(.*)good(.*)first(.*)issue(.*)");
    }
}
