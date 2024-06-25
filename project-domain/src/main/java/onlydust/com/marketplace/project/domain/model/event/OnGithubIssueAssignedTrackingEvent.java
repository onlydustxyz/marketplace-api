package onlydust.com.marketplace.project.domain.model.event;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.project.domain.model.ScoredApplication;
import onlydust.com.marketplace.project.domain.model.User;

import java.time.ZonedDateTime;
import java.util.Optional;
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

    public static OnGithubIssueAssignedTrackingEvent of(@NonNull OnGithubIssueAssigned onGithubIssueAssigned,
                                                        @NonNull Optional<User> user,
                                                        @NonNull Optional<ScoredApplication> scoredApplication) {
        return OnGithubIssueAssignedTrackingEvent.builder()
                .issueId(onGithubIssueAssigned.id())
                .assigneeGithubId(onGithubIssueAssigned.assigneeId())
                .assigneeUserId(user.map(User::getId).orElse(null))
                .createdAt(onGithubIssueAssigned.createdAt())
                .assignedAt(onGithubIssueAssigned.assignedAt())
                .isGoodFirstIssue(onGithubIssueAssigned.labels().stream().anyMatch(OnGithubIssueAssignedTrackingEvent::isGoodFirstIssue))
                .availabilityScore(scoredApplication.map(ScoredApplication::availabilityScore).orElse(null))
                .bestProjectsSimilarityScore(scoredApplication.map(ScoredApplication::bestProjectsSimilarityScore).orElse(null))
                .mainRepoLanguageUserScore(scoredApplication.map(ScoredApplication::mainRepoLanguageUserScore).orElse(null))
                .projectFidelityScore(scoredApplication.map(ScoredApplication::projectFidelityScore).orElse(null))
                .recommendationScore(scoredApplication.map(ScoredApplication::recommendationScore).orElse(null))
                .build();
    }

    private static boolean isGoodFirstIssue(String label) {
        return label.matches("(?i)(.*)good(.*)first(.*)issue(.*)");
    }
}
