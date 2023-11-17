package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubAccountMapper;
import onlydust.com.marketplace.api.postgres.adapter.mapper.GithubRepoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubAppInstallationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GithubRepoViewEntityRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresGithubAdapter implements GithubStoragePort {

    private final GithubAppInstallationRepository githubAppInstallationRepository;
    private final GithubRepoViewEntityRepository githubRepoViewEntityRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubAccount> findAccountByInstallationId(Long installationId) {
        return githubAppInstallationRepository.findById(installationId)
                .map(GithubAccountMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GithubRepo> findRepoById(Long repoId) {
        return githubRepoViewEntityRepository.findById(repoId)
                .map(GithubRepoMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubAccount> findInstalledAccountsByIds(List<Long> userGithubAccountIds) {
        return githubAppInstallationRepository.findAllByAccount_IdIn(userGithubAccountIds)
                .stream()
                .map(GithubAccountMapper::map)
                .toList();
    }
}
