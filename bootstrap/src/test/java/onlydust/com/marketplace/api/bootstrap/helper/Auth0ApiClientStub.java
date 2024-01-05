package onlydust.com.marketplace.api.bootstrap.helper;

import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.Map;

public class Auth0ApiClientStub implements GithubAuthenticationPort {

    private final Map<Long, String> pats = new java.util.HashMap<>();

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
        final var pat = pats.get(githubUserId);
        if (pat == null) {
            throw OnlyDustException.internalServerError(("No Github personal token for user %d in Auth0ApiClientStub").formatted(githubUserId));
        }
        return pat;
    }

    public void withPat(Long githubUserId, String pat) {
        pats.put(githubUserId, pat);
    }
}
