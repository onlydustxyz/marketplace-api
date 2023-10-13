package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Setter;

import java.util.Base64;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtVerifierStub implements JWTVerifier {

    @Setter
    private DecodedJWT decodedJWT;

    @Override
    public DecodedJWT verify(String token) throws JWTVerificationException {
        return decodedJWT;
    }

    @Override
    public DecodedJWT verify(DecodedJWT jwt) throws JWTVerificationException {
        return decodedJWT;
    }

    public void withJwtMock(Long githubUserId, String login, String avatarUrl) {
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(decodedJWT.getSubject()).thenReturn("github|" + githubUserId);
        when(decodedJWT.getPayload()).thenReturn(Base64.getUrlEncoder().encodeToString(String.format("""
                {
                  "nickname": "%s",
                  "picture": "%s",
                  "updated_at": "2023-10-10T13:55:48.308Z",
                  "iss": "https://onlydust-hackathon.eu.auth0.com/",
                  "aud": "62GDg2a6pCjnAln1FccD55eCKLJtj4T5",
                  "iat": 1696947933,
                  "exp": 2000000000,
                  "sub": "github|%d",
                  "sid": "21FFEt3yU2ESFcTtqW5xAilRFJ04auUb",
                  "nonce": "j4CwZI11uuV3tDzwq4UyDEKiWiIg-Z3fWWWUzp2UXIk"
                }
                """, login, avatarUrl, githubUserId).getBytes()));

        setDecodedJWT(decodedJWT);
    }
}
