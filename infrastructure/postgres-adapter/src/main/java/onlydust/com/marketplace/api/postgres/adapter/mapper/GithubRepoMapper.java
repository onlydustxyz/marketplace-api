package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoEntity;

public class GithubRepoMapper {
    public static GithubRepo map(GithubRepoEntity repo) {
        return new GithubRepo(
                repo.getId(),
                repo.getName(),
                repo.getHtmlUrl(),
                repo.getUpdatedAt(),
                repo.getDescription(),
                repo.getStarsCount(),
                repo.getForksCount()
        );
    }
}
