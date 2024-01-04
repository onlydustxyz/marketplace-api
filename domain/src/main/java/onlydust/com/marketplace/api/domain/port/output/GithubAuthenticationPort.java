package onlydust.com.marketplace.api.domain.port.output;

public interface GithubAuthenticationPort {

  String getGithubPersonalToken(Long githubUserId);
}
