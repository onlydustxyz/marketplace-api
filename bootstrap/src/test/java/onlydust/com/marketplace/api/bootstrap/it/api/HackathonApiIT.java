package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.SlackNotificationStub;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HackathonApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HackathonStoragePort hackathonStoragePort;
    @Autowired
    SlackNotificationStub slackNotificationStub;

    UserAuthHelper.AuthenticatedUser olivier;

    static Hackathon.Id hackathonId1;
    static Hackathon.Id hackathonId2;
    static Hackathon.Id hackathonId3;

    @BeforeEach
    void setUp() {
        olivier = userAuthHelper.authenticateOlivier();
    }

    void createHackathons() {
        final var startDate = ZonedDateTime.of(2024, 4, 19, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        final var hackathon1 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 1")
                .subtitle("Subtitle 1")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description("Description 1")
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon1.links().add(NamedLink.builder().url("https://www.google.com").value("Google").build());
        hackathon1.sponsorIds().addAll(List.of(UUID.fromString("0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa"),
                UUID.fromString("85435c9b-da7f-4670-bf65-02b84c5da7f0")));
        hackathon1.tracks().addAll(List.of(
                new Hackathon.Track("Track 1", "Subtitle 1", "Description 1", "icon-slug-1",
                        List.of(UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4"), UUID.fromString("7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"))),
                new Hackathon.Track("Track 2", "Subtitle 2", "Description 2", "icon-slug-2",
                        List.of(UUID.fromString("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")))
        ));

        final var hackathon2 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 2")
                .subtitle("Subtitle 2")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description("Description 2")
                .status(Hackathon.Status.DRAFT)
                .build();

        final var hackathon3 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 3")
                .subtitle("Subtitle 3")
                .location("Location 3")
                .startDate(startDate.plusDays(3))
                .endDate(startDate.plusDays(5))
                .description("Description 3")
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon1.links().add(NamedLink.builder().url("https://www.foo.org").value("Foo").build());
        hackathon1.sponsorIds().add(UUID.fromString("4202fd03-f316-458f-a642-421c7b3c7026"));
        hackathon1.tracks().add(
                new Hackathon.Track("Track 1 bis", "Subtitle 1 bis", "Description 1 bis", "icon-slug-1-bis",
                        List.of(UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4")))
        );

        hackathonStoragePort.save(hackathon1);
        hackathonStoragePort.save(hackathon2);
        hackathonStoragePort.save(hackathon3);
        hackathonId1 = hackathon1.id();
        hackathonId2 = hackathon2.id();
        hackathonId3 = hackathon3.id();
    }


    @Test
    @Order(1)
    void should_get_hackathon_by_slug() {
        createHackathons();

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-1")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId1.value().toString())
                .json("""
                        {
                          "slug": "hackathon-1",
                          "title": "Hackathon 1",
                          "subtitle": "Subtitle 1",
                          "description": "Description 1",
                          "location": null,
                          "totalBudget": null,
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-20T00:00:00Z",
                          "me": null,
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            },
                            {
                              "url": "https://www.foo.org",
                              "value": "Foo"
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
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                            }
                          ],
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
                            },
                            {
                              "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                              "slug": "red-bull",
                              "name": "Red bull",
                              "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A"
                            }
                          ],
                          "tracks": [
                            {
                              "name": "Track 1",
                              "subtitle": "Subtitle 1",
                              "description": "Description 1",
                              "iconSlug": "icon-slug-1",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null,
                                  "shortDescription": "Foundry like framework for starknet contracts",
                                  "visibility": "PUBLIC"
                                },
                                {
                                  "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                                  "slug": "cairo-streams",
                                  "name": "Cairo streams",
                                  "logoUrl": null,
                                  "shortDescription": "Stream library in cairo",
                                  "visibility": "PUBLIC"
                                }
                              ]
                            },
                            {
                              "name": "Track 2",
                              "subtitle": "Subtitle 2",
                              "description": "Description 2",
                              "iconSlug": "icon-slug-2",
                              "projects": [
                                {
                                  "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                                  "slug": "red-bull",
                                  "name": "Red bull",
                                  "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A",
                                  "shortDescription": "Red bull gives you wings!",
                                  "visibility": "PUBLIC"
                                }
                              ]
                            },
                            {
                              "name": "Track 1 bis",
                              "subtitle": "Subtitle 1 bis",
                              "description": "Description 1 bis",
                              "iconSlug": "icon-slug-1-bis",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null,
                                  "shortDescription": "Foundry like framework for starknet contracts",
                                  "visibility": "PUBLIC"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(10)
    void should_register_to_hackathon() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-1")))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.hasRegistered").isEqualTo(false);

        // When
        client.put()
                .uri(getApiURI(ME_PUT_HACKATHON_REGISTRATIONS.formatted(hackathonId1.value().toString())))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        assertEquals(1, slackNotificationStub.getHackathonNotifications().size());
        assertEquals(olivier.user().getId(), slackNotificationStub.getHackathonNotifications().get(0).getUserId());
        assertEquals(hackathonId1, slackNotificationStub.getHackathonNotifications().get(0).getHackathonId());

        // When
        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-1")))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.hasRegistered").isEqualTo(true);

        // When
        client.put()
                .uri(getApiURI(ME_PUT_HACKATHON_REGISTRATIONS.formatted(UUID.randomUUID().toString())))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();
    }

    @Test
    @Order(20)
    void should_get_all_published_hackathons() {
        // When
        client.get()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "hackathons": [
                            {
                              "slug": "hackathon-3",
                              "title": "Hackathon 3",
                              "location": "Location 3",
                              "startDate": "2024-04-22T00:00:00Z",
                              "endDate": "2024-04-24T00:00:00Z"
                            },
                            {
                              "slug": "hackathon-1",
                              "title": "Hackathon 1",
                              "location": null,
                              "startDate": "2024-04-19T00:00:00Z",
                              "endDate": "2024-04-20T00:00:00Z"
                            }
                          ]
                        }
                        """);
    }
}
