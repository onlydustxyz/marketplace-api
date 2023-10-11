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
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.UserClaims;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Auth0JwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JWTVerifier jwtVerifier;
    private final UserFacadePort userFacadePort;

    public Auth0JwtService(final Auth0Properties conf, UserFacadePort userFacadePort) {
        this.userFacadePort = userFacadePort;
        JwkProvider provider = new JwkProviderBuilder(conf.getJwksUrl())
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
                .acceptExpiresAt(conf.getExpiresAtLeeway())
                .withClaimPresence("nickname")
                .withClaimPresence("picture")
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

        final Long githubUserId = Long.valueOf(jwtClaims.getGithubWithUserId().replaceFirst("github\\|", ""));
        final User user = this.userFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(jwtClaims.getGithubLogin())
                .githubAvatarUrl(jwtClaims.getGithubAvatarUrl())
                .build());

        final UserClaims claims = UserClaims.builder()
                .userId(user.getId())
                .login(user.getLogin())
                .githubUserId(user.getGithubUserId())
                .avatarUrl(user.getAvatarUrl())
                //TODO
//                .isAnOnlydustAdmin(user.getIsAnOnlydustAdmin())
//                .projectsLeaded(user.getProjectsLeaded())
                .build();

        return Auth0Authentication.builder()
                //TODO add authorities from DB
                .authorities(Stream.of("me").map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(decodedJwt)
                .isAuthenticated(true)
                .claims(claims)
                .principal(decodedJwt.getSubject())
                .build();
    }

}
