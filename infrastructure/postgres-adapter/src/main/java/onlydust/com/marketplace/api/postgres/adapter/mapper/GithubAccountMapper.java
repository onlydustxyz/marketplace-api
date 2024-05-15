package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.project.domain.model.GithubAccount;

import java.util.stream.Collectors;

public interface GithubAccountMapper {
    static GithubAccount map(GithubAppInstallationViewEntity installation) {
        final GithubAccountViewEntity account = installation.getAccount();
        return GithubAccount.builder()
                .id(account.id())
                .login(account.login())
                .name(account.name())
                .type(account.type())
                .htmlUrl(account.htmlUrl())
                .avatarUrl(account.avatarUrl())
                .installed(installation.getSuspendedAt() == null)
                .installationId(installation.getId())
                .repos(account.repos().stream().filter(GithubRepoViewEntity::isPublic)
                        .map(GithubRepoMapper::map)
                        .collect(Collectors.toList()))
                .authorizedRepoIds(installation.getAuthorizedRepos()
                        .stream()
                        .map(githubAuthorizedRepoEntity -> githubAuthorizedRepoEntity.getId().getRepoId())
                        .toList())
                .build();
    }
}
