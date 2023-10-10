package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtClaims;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Slf4j
public class Auth0JwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JWTVerifier jwtVerifier;

    public Auth0JwtService(final String jwksUrl) {
        JwkProvider provider = new JwkProviderBuilder(jwksUrl)
                .build();

        RSAKeyProvider keyProvider = new RSAKeyProvider() {
            @SneakyThrows
            @Override
            public RSAPublicKey getPublicKeyById(String kid) {
                return (RSAPublicKey) provider.get(kid).getPublicKey();
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                // return the private key used
                return null;
            }

            @Override
            public String getPrivateKeyId() {
                return null;
            }
        };
        Algorithm algorithm = Algorithm.RSA256(keyProvider);

        this.jwtVerifier = JWT.require(algorithm)
                //TODO .withClaimPresence("scope")
                .build();
    }


    private static Auth0Authentication getUnauthenticatedWithExceptionFor(final int status,
                                                                          final String message,
                                                                          final Exception exception
    ) {
        return Auth0Authentication.builder()
                .onlydustException(OnlydustException
                        .builder()
                        .status(status)
                        .message(message)
                        .rootException(exception)
                        .build())
                .build();
    }

    public Auth0Authentication getAuthenticationFromJwt(final String jwt) {
        final DecodedJWT decodedJwt = this.jwtVerifier.verify(jwt);
        final Auth0JwtClaims jwtClaims;
        try {
            jwtClaims = objectMapper.readValue(Base64.getUrlDecoder().decode(decodedJwt.getPayload()),
                    Auth0JwtClaims.class);
        } catch (IOException e) {
            return getUnauthenticatedWithExceptionFor(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unable to deserialize Jwt token",
                    e);
        }

        final JwtClaims claims = JwtClaims.builder()
                .githubLogin(jwtClaims.getGithubLogin())
                .githubUserId(Long.valueOf(jwtClaims.getGithubWithUserId().replaceFirst("github\\|", "")))
                .build();

        return Auth0Authentication.builder()
                .credentials(decodedJwt)
                .isAuthenticated(true)
                .claims(claims)
                .principal(decodedJwt.getSubject())
                .build();
    }

}
