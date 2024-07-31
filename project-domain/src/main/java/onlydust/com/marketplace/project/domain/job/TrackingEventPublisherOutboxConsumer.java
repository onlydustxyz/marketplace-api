package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

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
                final var user = userStoragePort.getRegisteredUserByGithubId(onGithubIssueAssigned.assigneeId());
                trackingEventPublisher.publish(OnGithubIssueAssignedTrackingEvent.of(onGithubIssueAssigned, user.map(AuthenticatedUser::id)));
            }
        } else if (event instanceof OnPullRequestCreated onPullRequestCreated) {
            if (projectStoragePort.isLinkedToAProject(onPullRequestCreated.repoId()))
                userStoragePort.getRegisteredUserByGithubId(onPullRequestCreated.authorId())
                        .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestCreatedTrackingEvent.of(onPullRequestCreated, user.id())));
        } else if (event instanceof OnPullRequestMerged onPullRequestMerged) {
            if (projectStoragePort.isLinkedToAProject(onPullRequestMerged.repoId()))
                userStoragePort.getRegisteredUserByGithubId(onPullRequestMerged.authorId())
                        .ifPresent(user -> trackingEventPublisher.publish(OnPullRequestMergedTrackingEvent.of(onPullRequestMerged, user.id())));
        } else if (event instanceof OnApplicationCreated onApplicationCreated) {
            final var user = userStoragePort.getRegisteredUserByGithubId(onApplicationCreated.applicantId());
            trackingEventPublisher.publish(OnApplicationCreatedTrackingEvent.of(onApplicationCreated, user.map(AuthenticatedUser::id)));
        } else {
            trackingEventPublisher.publish(event);
        }
    }
}
