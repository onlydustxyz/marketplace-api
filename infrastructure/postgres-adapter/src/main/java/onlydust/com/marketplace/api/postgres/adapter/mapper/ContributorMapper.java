package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;

public interface ContributorMapper {

    static ContributorLinkView mapToContributorLinkView(GithubUserViewEntity user) {
        return ContributorLinkView.builder()
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }
}
