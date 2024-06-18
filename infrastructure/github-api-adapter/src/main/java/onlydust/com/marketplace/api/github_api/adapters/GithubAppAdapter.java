package onlydust.com.marketplace.api.github_api.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.kernel.infrastructure.github.GithubAppJwtBuilder;
import onlydust.com.marketplace.project.domain.port.output.GithubAppApiPort;

import java.util.Optional;

@AllArgsConstructor
public class GithubAppAdapter implements GithubAppApiPort {
    private final GithubHttpClient httpClient;
    private final GithubAppJwtBuilder jwtBuilder;

    @Override
    public Optional<String> getInstallationToken(Long installationId) {
        final var response = httpClient.post("/app/installations/" + installationId + "/access_tokens", jwtBuilder.generateSignedJwtToken(), Response.class);
        return response.map(Response::getToken);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class Response {
        String token;
    }
}
