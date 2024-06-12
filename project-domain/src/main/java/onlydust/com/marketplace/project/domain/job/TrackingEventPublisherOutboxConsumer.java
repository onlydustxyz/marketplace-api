package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.OnGithubIssueAssignedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestCreatedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestMergedTrackingEvent;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

@Slf4j
@AllArgsConstructor
public class TrackingEventPublisherOutboxConsumer implements OutboxConsumer {
    private final TrackingEventPublisher trackingEventPublisher;
    private final UserStoragePort userStoragePort;

    @Override
    public void process(Event event) {
        if (event instanceof OnGithubIssueAssigned onGithubIssueAssigned) {
            userStoragePort.getUserByGithubId(onGithubIssueAssigned.assigneeId())
                    .ifPresent(user -> trackingEventPublisher.publish(OnGithubIssueAssignedTrackingEvent.of(onGithubIssueAssigned, user)));
        } else if (event instanceof OnPullRequestCreated onPullRequestCreated) {
            userStoragePort.getUserByGithubId(onPullRequestCreated.authorId())
                    .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestCreatedTrackingEvent.of(onPullRequestCreated, user)));
        } else if (event instanceof OnPullRequestMerged onPullRequestMerged) {
            userStoragePort.getUserByGithubId(onPullRequestMerged.authorId())
                    .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestMergedTrackingEvent.of(onPullRequestMerged, user)));
        } else {
            trackingEventPublisher.publish(event);
        }
    }
}
