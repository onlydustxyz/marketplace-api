package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeHackathonApiIT extends AbstractMarketplaceBackOfficeApiIT {

    UserAuthHelper.AuthenticatedBackofficeUser camille;

    @BeforeEach
    void login() {
        camille = userAuthHelper.authenticateCamille();
    }

    @Test
    void should_raise_missing_authentication_given_no_access_token() {
        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_create_new_hackathon() {
        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "title": "Hackathon 2021",
                            "subtitle": "subtitle",
                            "startDate": "2024-01-01T00:00:00Z",
                            "endDate": "2024-01-05T00:00:00Z"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

}
