package onlydust.com.marketplace.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class GithubAccount {
    Long id;
    Long installationId;
    String login;
    String name;
    String type;
    String htmlUrl;
    String avatarUrl;
    @Builder.Default
    List<GithubRepo> repos = new ArrayList<>();
    @Builder.Default
    List<Long> authorizedRepoIds = new ArrayList<>();
    @Builder.Default
    Boolean installed = false;

    public List<GithubRepo> getAuthorizedRepos() {
        return this.repos.stream()
                .filter(githubRepo -> this.authorizedRepoIds.contains(githubRepo.getId()))
                .toList();
    }
}
