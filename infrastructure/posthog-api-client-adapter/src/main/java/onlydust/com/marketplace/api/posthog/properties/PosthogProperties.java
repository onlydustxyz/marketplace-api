package onlydust.com.marketplace.api.posthog.properties;

import lombok.Data;

@Data
public class PosthogProperties {
    String baseUri;
    String apiKey;
    String userAgent;
}
