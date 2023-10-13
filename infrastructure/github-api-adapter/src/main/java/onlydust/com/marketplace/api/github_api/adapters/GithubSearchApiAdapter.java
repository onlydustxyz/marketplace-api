package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.GithubUserSearchResponse;

import java.util.List;

@AllArgsConstructor
public class GithubSearchApiAdapter implements GithubSearchPort {
    private final GithubHttpClient client;

    @Override
    public List<GithubUserIdentity> searchUsersByLogin(String login) {
        return client.get("/search/users?per_page=5&q=" + login, GithubUserSearchResponse.class)
                .map(GithubUserSearchResponse::getItems)
                .orElse(List.of())
                .stream().map(
                        githubUser -> GithubUserIdentity.builder()
                                .githubUserId(githubUser.getId())
                                .githubLogin(githubUser.getLogin())
                                .githubAvatarUrl(githubUser.getAvatarUrl())
                                .build()
                ).toList();
    }
}
