package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.view.ShortRepoView;

public interface GithubRepoMapper {
    static ShortGithubRepoResponse mapRepoToShortResponse(GithubRepo githubRepo) {
        return new ShortGithubRepoResponse()
                .id(githubRepo.getId())
                .name(githubRepo.getName())
                .description(githubRepo.getDescription())
                .owner(githubRepo.getOwner())
                .htmlUrl(githubRepo.getHtmlUrl());
    }

    static ShortGithubRepoResponse mapRepoToShortResponse(ShortRepoView githubRepo) {
        return new ShortGithubRepoResponse()
                .id(githubRepo.getId())
                .name(githubRepo.getName())
                .description(githubRepo.getDescription())
                .owner(githubRepo.getOwner())
                .htmlUrl(githubRepo.getHtmlUrl());
    }
}
