package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.ScoredApplication;
import onlydust.com.marketplace.project.domain.model.event.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

import java.util.Comparator;

@Slf4j
@AllArgsConstructor
public class TrackingEventPublisherOutboxConsumer implements OutboxConsumer {
    private final TrackingEventPublisher trackingEventPublisher;
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;

    @Override
    public void process(Event event) {
        if (event instanceof OnGithubIssueAssigned onGithubIssueAssigned) {
            if (projectStoragePort.isLinkedToAProject(onGithubIssueAssigned.repoId())) {
                final var user = userStoragePort.getUserByGithubId(onGithubIssueAssigned.assigneeId());
                final var scoredApplication = userStoragePort.findScoredApplications(
                                onGithubIssueAssigned.assigneeId(),
                                GithubIssue.Id.of(onGithubIssueAssigned.id()))
                        .stream()
                        // We should only have 1 result
                        // except for the edge case of multiple project with same repo applications,
                        // hence taking the max should be enough
                        .max(Comparator.comparing(ScoredApplication::recommendationScore));
                trackingEventPublisher.publish(OnGithubIssueAssignedTrackingEvent.of(onGithubIssueAssigned, user, scoredApplication));
            }
        } else if (event instanceof OnPullRequestCreated onPullRequestCreated) {
            if (projectStoragePort.isLinkedToAProject(onPullRequestCreated.repoId()))
                userStoragePort.getUserByGithubId(onPullRequestCreated.authorId())
                        .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestCreatedTrackingEvent.of(onPullRequestCreated, user)));
        } else if (event instanceof OnPullRequestMerged onPullRequestMerged) {
            if (projectStoragePort.isLinkedToAProject(onPullRequestMerged.repoId()))
                userStoragePort.getUserByGithubId(onPullRequestMerged.authorId())
                        .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestMergedTrackingEvent.of(onPullRequestMerged, user)));
        } else if (event instanceof OnApplicationCreated onApplicationCreated) {
            userStoragePort.findScoredApplication(onApplicationCreated.applicationId())
                    .ifPresent(scoredApplication -> {
                        final var user = userStoragePort.getUserByGithubId(onApplicationCreated.applicantId());
                        trackingEventPublisher.publish(OnApplicationCreatedTrackingEvent.of(onApplicationCreated, scoredApplication, user));
                    });
        } else {
            trackingEventPublisher.publish(event);
        }
    }
}
