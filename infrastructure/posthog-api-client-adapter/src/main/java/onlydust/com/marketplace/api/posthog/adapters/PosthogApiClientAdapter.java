package onlydust.com.marketplace.api.posthog.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.TrackingRewardCreated;
import onlydust.com.marketplace.api.posthog.client.PosthogHttpClient;
import onlydust.com.marketplace.api.posthog.dto.EventDTO;
import onlydust.com.marketplace.api.posthog.processors.*;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.project.domain.model.event.OnApplicationCreatedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnGithubIssueAssignedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestCreatedTrackingEvent;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestMergedTrackingEvent;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;
import onlydust.com.marketplace.user.domain.event.UserSignedUp;

@AllArgsConstructor
@Slf4j
public class PosthogApiClientAdapter implements TrackingEventPublisher {
    PosthogProperties posthogProperties;
    PosthogHttpClient posthogHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(Event event) {
        if (event instanceof UserSignedUp userSignedUp)
            publish(new UserSignedUpEventReader(), userSignedUp);

        else if (event instanceof TrackingRewardCreated rewardCreated)
            publish(new TrackingRewardCreatedEventReader(), rewardCreated);

        else if (event instanceof OnGithubIssueAssignedTrackingEvent onGithubIssueAssigned)
            publish(new GithubIssueAssignedEventReader(), onGithubIssueAssigned);

        else if (event instanceof OnPullRequestCreatedTrackingEvent onPullRequestCreated)
            publish(new PullRequestCreatedEventReader(), onPullRequestCreated);

        else if (event instanceof OnPullRequestMergedTrackingEvent onPullRequestMerged)
            publish(new PullRequestMergedEventReader(), onPullRequestMerged);

        else if (event instanceof OnApplicationCreatedTrackingEvent onApplicationCreated)
            publish(new ApplicationCreatedEventReader(), onApplicationCreated);

        else
            LOGGER.debug("Event type {} not handle by Posthog event tracking consumer", event.getClass());
    }

    private <E extends Event> void publish(EventReader<E> reader, E event) {
        final var properties = objectMapper.createObjectNode();
        properties.put("$lib", posthogProperties.getUserAgent());
        reader.addProperties(event, properties);

        posthogHttpClient.send(
                "/capture/",
                HttpMethod.POST,
                EventDTO.builder()
                        .apiKey(posthogProperties.getApiKey())
                        .event(reader.eventType(event))
                        .distinctId(reader.distinctId(event).toString())
                        .timestamp(reader.timestamp(event).toString())
                        .properties(properties)
                        .build(),
                Void.class);
    }
}
