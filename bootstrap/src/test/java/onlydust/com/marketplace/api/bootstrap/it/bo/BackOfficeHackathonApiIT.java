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

        // When
        client.put()
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

    @Test
    @Order(10)
    void should_update_hackathon() {
        // When
        client.put()
                .uri(getApiURI(HACKATHONS_BY_ID.formatted(hackathonId1.getValue())))
                .header("Authorization", "Bearer " + camille.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-22T00:00:00Z",
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            }
                          ],
                          "sponsorIds": [
                            "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                            "85435c9b-da7f-4670-bf65-02b84c5da7f0"
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projectIds": [
                                "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projectIds": [
                                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projectIds": []
                            }
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.getValue())
                .json("""
                        {
                          "slug": "hackathon-2021-updated",
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-22T00:00:00Z",
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            }
                          ],
                          "sponsors": [
                            {
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projects": [
                                {
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "slug": "red-bull",
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projects": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(11)
    void should_get_updated_hackathon() {
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
                          "slug": "hackathon-2021-updated",
                          "status": "PUBLISHED",
                          "title": "Hackathon 2021 updated",
                          "subtitle": "subtitle updated",
                          "description": "My hackathon description",
                          "location": "Paris",
                          "totalBudget": "$1.000.000",
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-22T00:00:00Z",
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.facebook.com",
                              "value": "Facebook"
                            }
                          ],
                          "sponsors": [
                            {
                              "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                              "name": "Red Bull",
                              "url": "https://www.redbull.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "First track",
                              "subtitle": "First track subtitle",
                              "description": "First track description",
                              "iconSlug": "icon-1",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Second track",
                              "subtitle": "Second track subtitle",
                              "description": "Second track description",
                              "iconSlug": "icon-2",
                              "projects": [
                                {
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "slug": "red-bull",
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null
                                }
                              ]
                            },
                            {
                              "name": "Third track",
                              "subtitle": "Third track subtitle",
                              "description": "Third track description",
                              "iconSlug": "icon-3",
                              "projects": []
                            }
                          ]
                        }
                        """);
    }

}
