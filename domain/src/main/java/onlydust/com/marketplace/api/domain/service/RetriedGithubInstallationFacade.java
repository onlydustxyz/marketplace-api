package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.port.input.GithubInstallationFacadePort;

import java.util.Optional;

@AllArgsConstructor
public class RetriedGithubInstallationFacade implements GithubInstallationFacadePort {

    private final GithubInstallationFacadePort installationFacadePort;
    private final Config config;

    @Override
    public Optional<GithubAccount> getAccountByInstallationId(Long installationId) {
        for (int i = 0; i < config.retryCount; i++) {
            final var account = installationFacadePort.getAccountByInstallationId(installationId);
            if (account.isPresent()) {
                return account;
            }
            try {
                Thread.sleep(config.retryInterval);
            } catch (InterruptedException e) {
                throw OnlyDustException.builder().status(500).message("Error while retrying").build();
            }
        }
        return Optional.empty();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {
        Integer retryCount;
        Integer retryInterval;
    }
}
