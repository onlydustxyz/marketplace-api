package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtClaims;

import java.util.Base64;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtVerifierStub implements JWTVerifier {

    private final Map<String, DecodedJWT> decodedJWTPerToken = new java.util.HashMap<>();

    @Override
    public DecodedJWT verify(String token) throws JWTVerificationException {
        if (!decodedJWTPerToken.containsKey(token)) {
            throw new JWTVerificationException("Invalid token");
        }
        return decodedJWTPerToken.get(token);
    }

    @Override
    public DecodedJWT verify(DecodedJWT jwt) throws JWTVerificationException {
        return verify(jwt.getToken());
    }

    public String tokenFor(String githubWithUserId) {
        final String token = "token-for-%s".formatted(githubWithUserId);

        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn(githubWithUserId);
        when(decodedJWT.getToken()).thenReturn(token);
        when(decodedJWT.getPayload()).thenReturn(Base64.getUrlEncoder().encodeToString(String.format("""
                {
                  "iss": "https://onlydust-hackathon.eu.auth0.com/",
                  "aud": "62GDg2a6pCjnAln1FccD55eCKLJtj4T5",
                  "iat": 1696947933,
                  "exp": 2000000000,
                  "sub": "%s",
                  "azp": "gfOdiFOltYYUMYeBzNpeNAjMHmb9fWoV",
                  "scope": "openid profile email"
                }
                """, githubWithUserId).getBytes()));

        decodedJWTPerToken.put(token, decodedJWT);
        return token;
    }

    public String tokenFor(Long githubUserId) {
        return tokenFor("github|%d".formatted(githubUserId));
    }

    public String tokenFor(Auth0JwtClaims claims) {
        return tokenFor(claims.getGithubWithUserId());
    }
}
