package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributorSearchItemResponse;
import onlydust.com.marketplace.api.contract.model.ContributorSearchResponse;
import onlydust.com.marketplace.api.domain.model.Contributor;

import java.util.List;
import java.util.stream.Stream;

public class ContributorSearchResponseMapper {
    public static ContributorSearchResponse of(List<Contributor> internalContributors,
                                               List<Contributor> externalContributors) {
        return new ContributorSearchResponse()
                .contributors(Stream.concat(
                        internalContributors.stream().map(ContributorSearchResponseMapper::of),
                        externalContributors.stream().map(ContributorSearchResponseMapper::of)
                ).distinct().toList());
    }

    static ContributorSearchItemResponse of(Contributor contributor) {
        return new ContributorSearchItemResponse()
                .githubUserId(contributor.getId().getGithubUserId())
                .login(contributor.getId().getGithubLogin())
                .avatarUrl(contributor.getId().getGithubAvatarUrl())
                .isRegistered(contributor.getIsRegistered());
    }
}
