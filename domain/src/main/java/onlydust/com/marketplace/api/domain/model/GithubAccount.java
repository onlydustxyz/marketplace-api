package onlydust.com.marketplace.api.domain.model;

import java.util.List;

public record GithubAccount(
        Long id,
        String login,
        String type,
        String htmlUrl,
        String avatarUrl,
        List<GithubRepo> repos
) {
}
