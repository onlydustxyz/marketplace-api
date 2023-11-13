package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ShortGithubRepoResponse;
import onlydust.com.marketplace.api.domain.model.GithubRepo;

public interface GithubRepoMapper {
    static ShortGithubRepoResponse mapRepoToShortResponse(GithubRepo githubRepo) {
        return new ShortGithubRepoResponse()
                .id(githubRepo.getId())
                .name(githubRepo.getName())
                .description(githubRepo.getDescription())
                .owner(githubRepo.getOwner())
                .htmlUrl(githubRepo.getHtmlUrl());
    }
}
