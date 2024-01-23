package onlydust.com.marketplace.api.posthog.client;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class PosthogHttpClient extends onlydust.com.marketplace.kernel.infrastructure.HttpClient {

    PosthogProperties posthogProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder();
    }

    @Override
    protected URI uri(String path) {
        return URI.create(posthogProperties.getBaseUri() + path);
    }
}
