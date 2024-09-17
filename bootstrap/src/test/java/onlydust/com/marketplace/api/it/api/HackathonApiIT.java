package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.mockito.Mockito.verify;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HackathonApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HackathonStoragePort hackathonStoragePort;
    @Autowired
    SlackApiAdapter slackApiAdapter;

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
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description("Description 1")
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon1.githubLabels().addAll(List.of("label1", "label2"));
        hackathon1.communityLinks().add(NamedLink.builder().url("https://www.foo.bar").value("Foo").build());
        hackathon1.links().add(NamedLink.builder().url("https://www.google.com").value("Google").build());
        hackathon1.projectIds().addAll(List.of(UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4"),
                UUID.fromString("7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54"),
                UUID.fromString("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")));
        hackathon1.events().add(Hackathon.Event.builder()
                .id(UUID.randomUUID())
                .name("Event 1")
                .subtitle("Event 1 is awesome")
                .iconSlug("icon1")
                .startDate(startDate.plusHours(1))
                .endDate(startDate.plusHours(2))
                .links(Set.of(NamedLink.builder().url("https://www.foo.bar").value("Foo").build()))
                .build());
        hackathon1.events().add(Hackathon.Event.builder()
                .id(UUID.randomUUID())
                .name("Event 2")
                .subtitle("Event 2 is awesome")
                .iconSlug("icon2")
                .startDate(startDate.plusHours(2))
                .endDate(startDate.plusHours(3))
                .links(Set.of(NamedLink.builder().url("https://www.foo2.bar").value("Foo2").build()))
                .build());

        final var hackathon2 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 2")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description("Description 2")
                .status(Hackathon.Status.DRAFT)
                .build();

        final var hackathon3 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 3")
                .location("Location 3")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description("Description 3")
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon1.githubLabels().add("label1");
        hackathon1.communityLinks().add(NamedLink.builder().url("https://www.foo.bar").value("Bar").build());
        hackathon3.links().add(NamedLink.builder().url("https://www.foo.org").value("Foo").build());
        hackathon3.projectIds().add(UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4"));

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
                          "index": 1,
                          "title": "Hackathon 1",
                          "githubLabels": [
                            "label1",
                            "label2"
                          ],
                          "location": null,
                          "startDate": "2024-04-19T00:00:00Z",
                          "endDate": "2024-04-20T00:00:00Z",
                          "projects": [
                            {
                              "id": "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed",
                              "slug": "red-bull",
                              "name": "Red bull",
                              "logoUrl": "https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A",
                              "shortDescription": "Red bull gives you wings!",
                              "visibility": "PUBLIC",
                              "languages": []
                            },
                            {
                              "id": "7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54",
                              "slug": "cairo-streams",
                              "name": "Cairo streams",
                              "logoUrl": null,
                              "shortDescription": "Stream library in cairo",
                              "visibility": "PUBLIC",
                              "languages": []
                            },
                            {
                              "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                              "slug": "cairo-foundry",
                              "name": "Cairo foundry",
                              "logoUrl": null,
                              "shortDescription": "Foundry like framework for starknet contracts",
                              "visibility": "PUBLIC",
                              "languages": []
                            }
                          ],
                          "description": "Description 1",
                          "totalBudget": null,
                          "communityLinks": [
                            {
                              "url": "https://www.foo.bar",
                              "value": "Bar"
                            },
                            {
                              "url": "https://www.foo.bar",
                              "value": "Foo"
                            }
                          ],
                          "links": [
                            {
                              "url": "https://www.google.com",
                              "value": "Google"
                            }
                          ],
                          "me": null,
                          "subscriberCount": 0,
                          "issueCount": 0,
                          "openIssueCount": 0,
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 is awesome",
                              "iconSlug": "icon1",
                              "startDate": "2024-04-19T01:00:00Z",
                              "endDate": "2024-04-19T02:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.foo.bar",
                                  "value": "Foo"
                                }
                              ]
                            },
                            {
                              "name": "Event 2",
                              "subtitle": "Event 2 is awesome",
                              "iconSlug": "icon2",
                              "startDate": "2024-04-19T02:00:00Z",
                              "endDate": "2024-04-19T03:00:00Z",
                              "links": [
                                {
                                  "url": "https://www.foo2.bar",
                                  "value": "Foo2"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(2)
    void should_generate_unique_index_for_hackathons() {
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
                          "index": 1
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-2")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId2.value().toString())
                .json("""
                        {
                          "slug": "hackathon-2",
                          "index": 2
                        }
                        """);


        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-3")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(hackathonId3.value().toString())
                .json("""
                        {
                          "slug": "hackathon-3",
                          "index": 3
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

        verify(slackApiAdapter).onUserRegistration(hackathonId1, olivier.userId());

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
                              "slug": "hackathon-1",
                              "index": 1,
                              "title": "Hackathon 1",
                              "githubLabels": [
                                "label1",
                                "label2"
                              ],
                              "location": null,
                              "startDate": "2024-04-19T00:00:00Z",
                              "endDate": "2024-04-20T00:00:00Z",
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
                                },
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                }
                              ],
                              "subscriberCount": 1,
                              "issueCount": 0,
                              "openIssueCount": 0
                            },
                            {
                              "slug": "hackathon-3",
                              "index": 3,
                              "title": "Hackathon 3",
                              "githubLabels": [],
                              "location": "Location 3",
                              "startDate": "2024-04-19T00:00:00Z",
                              "endDate": "2024-04-20T00:00:00Z",
                              "projects": [
                                {
                                  "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                  "slug": "cairo-foundry",
                                  "name": "Cairo foundry",
                                  "logoUrl": null
                                }
                              ],
                              "subscriberCount": 0,
                              "issueCount": 0,
                              "openIssueCount": 0
                            }
                          ]
                        }
                        """);
    }
}
