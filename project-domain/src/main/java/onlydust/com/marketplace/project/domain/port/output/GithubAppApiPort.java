package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.GithubAppAccessToken;

import java.util.Optional;

public interface GithubAppApiPort {
    Optional<GithubAppAccessToken> getInstallationToken(Long installationId);
}
