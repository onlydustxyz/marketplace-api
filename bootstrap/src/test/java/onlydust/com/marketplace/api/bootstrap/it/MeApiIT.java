package onlydust.com.marketplace.api.bootstrap.it;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Base64;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    JWTVerifier jwtVerifier;

    @Test
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(ME_GET))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

//    @Test
//    void should_get_current_user_given_a_valid_hasura_jwt() throws JsonProcessingException {
//        // Given
//        final UUID userId = UUID.randomUUID();
//        final long githubUserId = faker.number().randomNumber();
//        final String hasuraJwt = JwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
//                .sub(userId.toString())
//                .iss(jwtSecret.getIssuer())
//                .claims(HasuraJwtPayload.HasuraClaims.builder()
//                        .githubUserId(githubUserId)
//                        .userId(userId)
//                        .allowedRoles(List.of(faker.pokemon().name(), "me"))
//                        .build())
//                .build());
//
//        // When
//        client.get()
//                .uri(getApiURI(ME_GET))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + hasuraJwt)
//                // Then
//                .exchange()
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(userId.toString())
//                .jsonPath("$.githubUserId").isEqualTo(githubUserId);
//    }


    @Test
    void should_get_current_user_given_a_valid_jwt() {
        // Given
        final Long githubUserId = faker.number().randomNumber();
        final String login = faker.name().username();
        final String avatarUrl = faker.internet().avatar();

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

        ((JwtVerifierStub) jwtVerifier).setDecodedJWT(decodedJWT);

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer fake-jwt")
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.id").isNotEmpty();
    }
}
