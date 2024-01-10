package onlydust.com.marketplace.api.auth0.api.client.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.auth0.api.client.adapter.authentication.Auth0ApiAuthenticator;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.net.URI;
import java.net.http.HttpRequest;

@AllArgsConstructor
public class Auth0ApiHttpClient extends HttpClient {
    private final Auth0ApiClientProperties properties;
    private final Auth0ApiAuthenticator auth0ApiAuthenticator;

    @Override
    protected HttpRequest.Builder builder() {
        try {
            return HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + auth0ApiAuthenticator.getAuth0ManagementApiAccessToken());
        } catch (Exception e) {
            throw OnlyDustException.internalServerError("Error when building Auth0 authorization header", e);
        }
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.getDomainBaseUri() + path);
    }
}
