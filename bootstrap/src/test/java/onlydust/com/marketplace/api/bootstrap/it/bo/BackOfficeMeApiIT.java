package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagBO;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoEcosystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@TagBO
public class BackOfficeMeApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    BoEcosystemRepository ecosystemRepository;

    String googleId;
    String name;
    String avatarUrl;
    String email;
    String token;

    @BeforeEach
    void setup() {
        googleId = faker.number().randomNumber(15, true) + "";
        final var sub = "google-oauth2|%s".formatted(googleId);
        name = faker.name().name();
        avatarUrl = faker.internet().avatar();
        email = faker.internet().emailAddress();

        token = ((JwtVerifierStub) jwtVerifier).tokenFor(sub, 50000L);
        userAuthHelper.mockAuth0UserInfo(sub, faker.name().username(), name, avatarUrl, email);
    }

    @Test
    void should_raise_missing_authentication_given_no_access_token() {
        // When
        client.get()
                .uri(getApiURI(GET_ME))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_return_my_information() {
        // When
        client.get()
                .uri(getApiURI(GET_ME))
                .header("Authorization", "Bearer " + token)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                            "avatarUrl": "%s",
                            "name": "%s",
                            "email": "%s",
                            "roles": ["BO_READER"]
                        }
                        """.formatted(avatarUrl, name, email));
    }

}
