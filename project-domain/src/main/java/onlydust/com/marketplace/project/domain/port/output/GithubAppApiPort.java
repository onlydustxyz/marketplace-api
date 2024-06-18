package onlydust.com.marketplace.project.domain.port.output;

import java.util.Optional;

public interface GithubAppApiPort {
    Optional<String> getInstallationToken(Long installationId);
}
