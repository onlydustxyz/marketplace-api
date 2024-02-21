package onlydust.com.marketplace.project.domain.port.output;

public interface GithubAuthenticationPort {
    String getGithubPersonalToken(Long githubUserId);
}
