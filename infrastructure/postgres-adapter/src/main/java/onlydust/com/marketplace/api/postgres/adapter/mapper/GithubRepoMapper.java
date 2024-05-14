package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.project.domain.model.GithubRepo;

public interface GithubRepoMapper {
    static GithubRepo map(GithubRepoViewEntity repo) {
        return GithubRepo.builder()
                .id(repo.getId())
                .owner(repo.getOwner().login())
                .name(repo.getName())
                .htmlUrl(repo.getHtmlUrl())
                .updatedAt(repo.getUpdatedAt())
                .description(repo.getDescription())
                .starsCount(repo.getStarsCount())
                .forksCount(repo.getForksCount())
                .build();
    }

    static GithubRepo map(onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoViewEntity repo) {
        return GithubRepo.builder()
                .id(repo.getId())
                .owner(repo.getOwnerLogin())
                .name(repo.getName())
                .htmlUrl(repo.getHtmlUrl())
                .updatedAt(repo.getUpdatedAt())
                .description(repo.getDescription())
                .starsCount(repo.getStarsCount())
                .forksCount(repo.getForksCount())
                .build();
    }
}
