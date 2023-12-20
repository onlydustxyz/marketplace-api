package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

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

    public void withJwtMock(String token, Long githubUserId, String login, String avatarUrl, String email) {
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("github|" + githubUserId);
        when(decodedJWT.getPayload()).thenReturn(Base64.getUrlEncoder().encodeToString(String.format("""
                {
                  "nickname": "%s",
                  "picture": "%s",
                  "email": "%s",
                  "updated_at": "2023-10-10T13:55:48.308Z",
                  "iss": "https://onlydust-hackathon.eu.auth0.com/",
                  "aud": "62GDg2a6pCjnAln1FccD55eCKLJtj4T5",
                  "iat": 1696947933,
                  "exp": 2000000000,
                  "sub": "github|%d",
                  "sid": "21FFEt3yU2ESFcTtqW5xAilRFJ04auUb",
                  "nonce": "j4CwZI11uuV3tDzwq4UyDEKiWiIg-Z3fWWWUzp2UXIk"
                }
                """, login, avatarUrl, email, githubUserId).getBytes()));

        decodedJWTPerToken.put(token, decodedJWT);
    }
}
