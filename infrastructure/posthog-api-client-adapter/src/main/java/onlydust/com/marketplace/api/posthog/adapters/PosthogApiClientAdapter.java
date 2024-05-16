package onlydust.com.marketplace.api.posthog.adapters;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.posthog.client.PosthogHttpClient;
import onlydust.com.marketplace.api.posthog.dto.EventDTO;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;

@AllArgsConstructor
@Slf4j
public class PosthogApiClientAdapter implements OutboxConsumer {
    PosthogProperties posthogProperties;
    PosthogHttpClient posthogHttpClient;

    @Override
    public void process(Event event) {
        if (event instanceof UserSignedUp userSignedUp) {
            posthogHttpClient.send("/capture/", HttpMethod.POST,
                    EventDTO.builder()
                            .apiKey(posthogProperties.getApiKey())
                            .event("user_signed_up")
                            .distinctId(userSignedUp.getUserId().toString())
                            .timestamp(userSignedUp.getSignedUpAt().toInstant().toString())
                            .properties(EventDTO.PropertiesDTO.builder()
                                    .userAgent(posthogProperties.getUserAgent())
                                    .build())
                            .build(),
                    Void.class);
        } else {
            LOGGER.warn("Event type {} not handle by Posthog event tracking consumer", event.getClass());
        }
    }
}
