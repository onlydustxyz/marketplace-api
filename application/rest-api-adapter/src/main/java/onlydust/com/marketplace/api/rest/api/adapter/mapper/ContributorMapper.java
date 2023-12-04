package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;

import java.net.URI;

public interface ContributorMapper {
    static ContributorResponse of(ContributorLinkView contributorLinkView) {
        return new ContributorResponse()
                .githubUserId(contributorLinkView.getGithubUserId())
                .login(contributorLinkView.getLogin())
                .avatarUrl(contributorLinkView.getAvatarUrl())
                .htmlUrl(URI.create(contributorLinkView.getUrl()))
                .isRegistered(contributorLinkView.getIsRegistered());
    }
}
