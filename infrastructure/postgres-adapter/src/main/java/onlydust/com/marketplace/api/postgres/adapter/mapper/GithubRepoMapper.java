package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoEntity;

public interface GithubRepoMapper {

  static GithubRepo map(GithubRepoEntity repo) {
    return GithubRepo.builder()
        .id(repo.getId())
        .owner(repo.getOwner().getLogin())
        .name(repo.getName())
        .htmlUrl(repo.getHtmlUrl())
        .updatedAt(repo.getUpdatedAt())
        .description(repo.getDescription())
        .starsCount(repo.getStarsCount())
        .forksCount(repo.getForksCount())
        .build();
  }

  static GithubRepo map(GithubRepoViewEntity repo) {
    return GithubRepo.builder()
        .id(repo.getId())
        .owner(repo.getOwner())
        .name(repo.getName())
        .htmlUrl(repo.getHtmlUrl())
        .updatedAt(repo.getUpdatedAt())
        .description(repo.getDescription())
        .starsCount(repo.getStarsCount())
        .forksCount(repo.getForksCount())
        .build();
  }
}
