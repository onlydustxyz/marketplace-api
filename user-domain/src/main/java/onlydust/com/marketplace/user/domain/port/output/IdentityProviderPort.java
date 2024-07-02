package onlydust.com.marketplace.user.domain.port.output;

public interface IdentityProviderPort {

    void deleteUser(Long githubUserId);

    String getGithubPersonalToken(Long githubUserId);
}
