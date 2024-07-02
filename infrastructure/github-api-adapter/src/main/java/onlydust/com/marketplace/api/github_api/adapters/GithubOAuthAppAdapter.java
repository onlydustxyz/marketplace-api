package onlydust.com.marketplace.api.github_api.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.ApplicantGrantDTO;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.user.domain.port.output.GithubOAuthAppPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@AllArgsConstructor
public class GithubOAuthAppAdapter implements GithubOAuthAppPort {

    private final GithubHttpClient.Config config;

    @Override
    public void deleteGithubOAuthApp(String githubOAuthAppId, String githubOAuthAppSecret, String personalAccessToken) {
        try {
            final HttpClient httpClient = HttpClient.newHttpClient();
            final HttpRequest httpRequest;
            httpRequest = HttpRequest.newBuilder().uri(URI.create("%s/applications/%s/grant".formatted(config.getBaseUri(), githubOAuthAppId)))
                    .header("Authorization", getBasicAuthenticationHeader(githubOAuthAppId, githubOAuthAppSecret))
                    .method(HttpMethod.DELETE.name(),
                            HttpRequest.BodyPublishers.ofByteArray(new ObjectMapper().writeValueAsBytes(ApplicantGrantDTO.builder().accessToken(personalAccessToken).build())))
                    .build();
            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() > 399) {
                throw OnlyDustException.internalServerError("Failed to delete Github OAuth App integration : %s".formatted(httpResponse.body()));
            }
        } catch (IOException | InterruptedException e) {
            throw OnlyDustException.internalServerError("Failed to delete Github OAuth App integration", e);
        }
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
