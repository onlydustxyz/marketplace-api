package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubAccountMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresGithubAdapter implements GithubStoragePort {

    private final GithubAppInstallationRepository githubAppInstallationRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubAccount> findAccountByInstallationId(Long installationId) {
        return githubAppInstallationRepository.findById(installationId)
                .map(installation -> GithubAccountMapper.map(installation.getAccount()));
    }
}
