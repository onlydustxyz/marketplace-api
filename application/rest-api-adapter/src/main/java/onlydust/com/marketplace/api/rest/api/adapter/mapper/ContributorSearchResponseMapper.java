package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.ContributorSearchResponse;
import onlydust.com.marketplace.project.domain.model.Contributor;

import java.util.List;

public class ContributorSearchResponseMapper {
    public static ContributorSearchResponse of(List<Contributor> internalContributors,
                                               List<Contributor> externalContributors) {
        return new ContributorSearchResponse()
                .internalContributors(
                        internalContributors.stream()
                                .map(ContributorSearchResponseMapper::of).distinct().toList()
                )
                .externalContributors(
                        externalContributors.stream()
                                .filter(contributor -> !internalContributors.contains(contributor))
                                .map(ContributorSearchResponseMapper::of).distinct().toList()
                );
    }

    static ContributorResponse of(Contributor contributor) {
        return new ContributorResponse()
                .githubUserId(contributor.getId().getGithubUserId())
                .login(contributor.getId().getGithubLogin())
                .avatarUrl(contributor.getId().getGithubAvatarUrl())
                .isRegistered(contributor.getIsRegistered());
    }
}
