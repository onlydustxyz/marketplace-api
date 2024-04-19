package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeHackathonApiIT extends AbstractMarketplaceBackOfficeApiIT {

    final static MutableObject<String> hackathonId1 = new MutableObject<>();
    final static MutableObject<String> hackathonId2 = new MutableObject<>();

    UserAuthHelper.AuthenticatedBackofficeUser camille;

    @BeforeEach
    void login() {
        camille = userAuthHelper.authenticateCamille();
    }

    @Test
    @Order(1)
    void should_raise_missing_authentication_given_no_access_token() {
        // When
        client.post()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .exchange()
                .expectStatus()
                // Then
                .isUnauthorized();
    }

    @Test
    @Order(2)
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
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(hackathonId1::setValue)
                .json("""
                        {
                            "slug": "hackathon-2021",
                            "status": "DRAFT",
                            "title": "Hackathon 2021",
                            "subtitle": "subtitle",
                            "description": null,
                            "location": null,
                            "totalBudget": null,
                            "startDate": "2024-01-01T00:00:00Z",
                            "endDate": "2024-01-05T00:00:00Z",
                            "links": [],
                            "sponsors": [],
                            "tracks": []
                        }
                        """);
        assertThat(hackathonId1.getValue()).isNotEmpty();
        assertThat(UUID.fromString(hackathonId1.getValue())).isNotNull();
    }

    @Test
    @Order(3)
    void should_get_new_hackathon() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.getValue())
                .json("""
                        {
                            "slug": "hackathon-2021",
                            "status": "DRAFT",
                            "title": "Hackathon 2021",
                            "subtitle": "subtitle",
                            "description": null,
                            "location": null,
                            "totalBudget": null,
                            "startDate": "2024-01-01T00:00:00Z",
                            "endDate": "2024-01-05T00:00:00Z",
                            "links": [],
                            "sponsors": [],
                            "tracks": []
                        }
                        """);
    }

}
