package onlydust.com.marketplace.api.postgres.adapter.mapper;

import java.util.stream.Collectors;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;

public interface GithubAccountMapper {

  static GithubAccount map(GithubAppInstallationEntity installation) {
    final GithubAccountEntity account = installation.getAccount();
    return GithubAccount.builder()
        .id(account.getId())
        .login(account.getLogin())
        .name(account.getName())
        .type(account.getType())
        .htmlUrl(account.getHtmlUrl())
        .avatarUrl(account.getAvatarUrl())
        .installed(installation.getSuspendedAt() == null)
        .installationId(installation.getId())
        .repos(account.getRepos().stream().filter(GithubRepoEntity::isPublic)
            .map(GithubRepoMapper::map)
            .collect(Collectors.toList()))
        .authorizedRepoIds(installation.getAuthorizedRepos()
            .stream()
            .map(githubAuthorizedRepoEntity -> githubAuthorizedRepoEntity.getId().getRepoId())
            .toList())
        .build();
  }
}
