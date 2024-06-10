package onlydust.com.marketplace.api.posthog.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.TrackingRewardCreated;
import onlydust.com.marketplace.api.posthog.client.PosthogHttpClient;
import onlydust.com.marketplace.api.posthog.dto.EventDTO;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class PosthogApiClientAdapter implements OutboxConsumer {
    PosthogProperties posthogProperties;
    PosthogHttpClient posthogHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Event event) {
        if (event instanceof UserSignedUp userSignedUp) {
            final ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("$lib", posthogProperties.getUserAgent());
            posthogHttpClient.send("/capture/", HttpMethod.POST,
                    EventDTO.builder()
                            .apiKey(posthogProperties.getApiKey())
                            .event("user_signed_up")
                            .distinctId(userSignedUp.getUserId().toString())
                            .timestamp(userSignedUp.getSignedUpAt().toInstant().toString())
                            .properties(objectNode)
                            .build(),
                    Void.class);
        } else if (event instanceof TrackingRewardCreated rewardCreated) {
            final ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("amount", rewardCreated.amount().doubleValue());
            objectNode.put("count_contributions", rewardCreated.contributionsCount());
            objectNode.put("project_id", rewardCreated.projectId().toString());
            objectNode.put("sender_id", rewardCreated.senderId().toString());
            objectNode.put("currency", rewardCreated.currencyCode());
            objectNode.put("contributions_count", rewardCreated.contributionsCount());
            objectNode.put("$lib", posthogProperties.getUserAgent());
            if (Objects.nonNull(rewardCreated.dollarsEquivalent())) {
                objectNode.put("amount_in_dollars", rewardCreated.dollarsEquivalent().doubleValue());
            }
            posthogHttpClient.send("/capture/", HttpMethod.POST,
                    EventDTO.builder()
                            .apiKey(posthogProperties.getApiKey())
                            .distinctId(Objects.isNull(rewardCreated.recipientId()) ? UUID.randomUUID().toString() : rewardCreated.recipientId().toString())
                            .event("reward_received")
                            .timestamp(Instant.now().toString())
                            .properties(objectNode)
                            .build(),
                    Void.class
            );
        } else {
            LOGGER.warn("Event type {} not handle by Posthog event tracking consumer", event.getClass());
        }
    }

}
