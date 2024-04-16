package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtVerifierStub implements JWTVerifier {

    private final Map<String, DecodedJWT> decodedJWTPerToken = Collections.synchronizedMap(new HashMap<>());

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

    public String tokenFor(String sub, long expiresInMilliseconds) {
        final String token = "token-for-%s".formatted(sub);

        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn(sub);
        when(decodedJWT.getToken()).thenReturn(token);
        when(decodedJWT.getPayload()).thenReturn(Base64.getUrlEncoder().encodeToString(String.format("""
                        {
                          "iss": "https://onlydust-hackathon.eu.auth0.com/",
                          "aud": "62GDg2a6pCjnAln1FccD55eCKLJtj4T5",
                          "iat": 1696947933,
                          "exp": %d,
                          "sub": "%s",
                          "azp": "gfOdiFOltYYUMYeBzNpeNAjMHmb9fWoV",
                          "scope": "openid profile email"
                        }
                        """,
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() + expiresInMilliseconds),
                sub).getBytes()));

        decodedJWTPerToken.put(token, decodedJWT);
        return token;
    }

    public String tokenFor(String sub) {
        return tokenFor(sub, 100_000L);
    }

    public String tokenFor(Long githubUserId) {
        return tokenFor("github|%d".formatted(githubUserId));
    }

    public String googleTokenFor(String email) {
        return tokenFor("google-oauth2|%s".formatted(email));
    }

    public String tokenFor(Long githubUserId, long expiresInMilliseconds) {
        return tokenFor("github|%d".formatted(githubUserId), expiresInMilliseconds);
    }
}
