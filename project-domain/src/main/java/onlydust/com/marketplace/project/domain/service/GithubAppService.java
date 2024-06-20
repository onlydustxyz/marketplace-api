package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.GithubAppAccessToken;
import onlydust.com.marketplace.project.domain.port.output.GithubAppApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;

import java.util.Optional;

@AllArgsConstructor
public class GithubAppService {
    private final GithubStoragePort githubStoragePort;
    private final GithubAppApiPort githubAppApiPort;

    public Optional<GithubAppAccessToken> getInstallationTokenFor(Long repoId) {
        return githubStoragePort.findInstallationIdByRepoId(repoId)
                .flatMap(githubAppApiPort::getInstallationToken);
    }
}
