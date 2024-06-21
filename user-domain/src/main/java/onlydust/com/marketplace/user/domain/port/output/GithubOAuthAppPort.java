package onlydust.com.marketplace.user.domain.port.output;

public interface GithubOAuthAppPort {

    void deleteGithubOAuthApp(String githubOAuthAppId, String githubOAuthAppSecret, String personalAccessToken);
}
