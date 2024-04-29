package onlydust.com.marketplace.api.node.guardians;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class NodeGuardiansHttpClient extends HttpClient {

    private final NodeGuardiansApiProperties nodeGuardiansApiProperties;

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder().header("Authorization", "Bearer %s".formatted(nodeGuardiansApiProperties.getApiKey()));
    }

    @Override
    protected URI uri(String path) {
        return URI.create(nodeGuardiansApiProperties.getBaseUri() + path);
    }
}
