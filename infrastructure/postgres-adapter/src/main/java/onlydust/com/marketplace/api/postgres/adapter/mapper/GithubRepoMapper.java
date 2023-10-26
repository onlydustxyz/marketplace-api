package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.GithubRepoViewEntity;

public class GithubRepoMapper {
    public static GithubRepo map(GithubRepoEntity repo) {
        return new GithubRepo(
                repo.getId(),
                repo.getOwner().getLogin(),
                repo.getName(),
                repo.getHtmlUrl(),
                repo.getUpdatedAt(),
                repo.getDescription(),
                repo.getStarsCount(),
                repo.getForksCount()
        );
    }

    public static GithubRepo map(GithubRepoViewEntity repo) {
        return new GithubRepo(
                repo.getId(),
                repo.getOwner(),
                repo.getName(),
                repo.getHtmlUrl(),
                repo.getUpdatedAt(),
                repo.getDescription(),
                repo.getStarsCount(),
                repo.getForksCount()
        );
    }
}
