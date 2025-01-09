package onlydust.com.marketplace.api.it.api;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;

@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HackathonApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HackathonStoragePort hackathonStoragePort;
    @Autowired
    SlackApiAdapter slackApiAdapter;

    UserAuthHelper.AuthenticatedUser olivier;

    private static final AtomicBoolean setupDone = new AtomicBoolean();

    static Hackathon hackathon1;
    static Hackathon.Id hackathonId1;
    static Hackathon.Id hackathonId2;
    static Hackathon.Id hackathonId3;

    static ProjectId projectId1 = ProjectId.of("8156fc5f-cec5-4f70-a0de-c368772edcd4");
    static ProjectId projectId2 = ProjectId.of("7ce1a761-2b7b-43ba-9eb5-17e95ef4aa54");
    static ProjectId projectId3 = ProjectId.of("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed");

    @BeforeEach
    void setUp() {
        olivier = userAuthHelper.authenticateOlivier();

        if (setupDone.compareAndExchange(false, true)) return;

        final var startDate = ZonedDateTime.of(2024, 4, 19, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        hackathon1 = Hackathon.builder()
                .id(Hackathon.Id.random())
                .title("Hackathon 1")
                .startDate(startDate)
                .endDate(startDate.plusDays(7))
                .description("Description 1")
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon1.githubLabels().addAll(List.of("label1", "label2"));
        hackathon1.communityLinks().add(NamedLink.builder().url("https://www.foo.bar").value("Foo").build());
        hackathon1.links().add(NamedLink.builder().url("https://www.google.com").value("Google").build());
        hackathon1.projectIds().addAll(List.of(projectId1.value(), projectId2.value(), projectId3.value()));
        hackathon1.events().add(Hackathon.Event.builder()
                .id(UUID.randomUUID())
                .name("Event 1")
                .subtitle("Event 1 is awesome")
                .iconSlug("icon1")
                .startDate(startDate.plusDays(1))
                .endDate(startDate.plusDays(2))
                .links(Set.of(NamedLink.builder().url("https://www.foo.bar").value("Foo").build()))
                .build());
        hackathon1.events().add(Hackathon.Event.builder()
                .id(UUID.randomUUID())
                .name("Event 2")
                .subtitle("Event 2 is awesome")
                .iconSlug("icon2")
                .startDate(startDate.plusDays(2))
                .endDate(startDate.plusDays(3))
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
        hackathon3.projectIds().add(projectId1.value());

        final var repo = githubHelper.createRepo(projectId2);
        final var labelId = githubHelper.createLabel("label1");
        // 3 available issues
        IntStream.range(0, 3).forEach(i -> {
            final var issue = githubHelper.createIssue(repo, ZonedDateTime.now().minusDays(1), null, "OPEN", olivier);
            githubHelper.addLabelToIssue(issue.id(), labelId, ZonedDateTime.now());
        });
        // 2 non available issues
        IntStream.range(0, 2).forEach(i -> {
            final var issue = githubHelper.createIssue(repo, ZonedDateTime.now().minusDays(1), null, "OPEN", olivier);
            githubHelper.addLabelToIssue(issue.id(), labelId, ZonedDateTime.now());
            githubHelper.assignIssueToContributor(issue.id(), olivier.githubUserId().value());
        });

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
                          "endDate": "2024-04-26T00:00:00Z",
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
                          "subscriberCount": 0,
                          "issueCount": 5,
                          "openIssueCount": 3,
                          "events": [
                            {
                              "name": "Event 1",
                              "subtitle": "Event 1 is awesome",
                              "iconSlug": "icon1",
                              "startDate": "2024-04-20T00:00:00Z",
                              "endDate": "2024-04-21T00:00:00Z",
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
                              "startDate": "2024-04-21T00:00:00Z",
                              "endDate": "2024-04-22T00:00:00Z",
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
                .uri(getApiURI(ME_HACKATHON_REGISTRATIONS.formatted(hackathonId1.value().toString())))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isRegistered").isEqualTo(false);

        // When
        client.put()
                .uri(getApiURI(ME_HACKATHON_REGISTRATIONS.formatted(hackathonId1.value().toString())))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        verify(slackApiAdapter).onUserRegistration(hackathonId1, olivier.userId());

        // When
        client.get()
                .uri(getApiURI(ME_HACKATHON_REGISTRATIONS.formatted(hackathonId1.value().toString())))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isRegistered").isEqualTo(true);

        // When
        client.put()
                .uri(getApiURI(ME_HACKATHON_REGISTRATIONS.formatted(UUID.randomUUID().toString())))
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
                              "endDate": "2024-04-26T00:00:00Z",
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
                              "issueCount": 5,
                              "openIssueCount": 3
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

    @Test
    @Order(30)
    void should_get_hackathon_by_slug_v2() {
        // When
        final var response = client.get()
                .uri(getApiURI(HACKATHONS_V2_BY_SLUG.formatted(hackathon1.slug())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonResponseV2.class)
                .returnResult().getResponseBody();

        assertThat(response.getId()).isEqualTo(hackathonId1.value());
        assertThat(response.getSlug()).isEqualTo(hackathon1.slug());
        assertThat(response.getTitle()).isEqualTo(hackathon1.title());
        assertThat(response.getStartDate()).isEqualTo(hackathon1.startDate());
        assertThat(response.getEndDate()).isEqualTo(hackathon1.endDate());
        assertThat(response.getDescription()).isEqualTo(hackathon1.description());
        assertThat(response.getLocation()).isEqualTo(hackathon1.location());
        assertThat(response.getProjectCount()).isEqualTo(hackathon1.projectIds().size());
        assertThat(response.getSubscriberCount()).isEqualTo(1);
        assertThat(response.getIssueCount()).isEqualTo(5);
        assertThat(response.getAvailableIssueCount()).isEqualTo(3);
        assertThat(response.getCommunityLinks())
            .extracting(SimpleLink::getUrl)
            .containsExactlyInAnyOrderElementsOf(hackathon1.communityLinks().stream()
                    .map(NamedLink::getUrl)
                    .toList());
        assertThat(response.getLinks())
            .extracting(SimpleLink::getUrl)
            .containsExactlyInAnyOrderElementsOf(hackathon1.links().stream()
                    .map(NamedLink::getUrl)
                    .toList());
    }

    @Test
    @Order(30)
    void should_get_hackathon_projects() {
        // When
        final var projects = client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG_PROJECTS.formatted(hackathon1.slug())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectPageResponseV2.class)
                .returnResult().getResponseBody().getProjects();

        assertThat(projects)
            .hasSize(hackathon1.projectIds().size())
            .extracting(ProjectShortResponseV2::getId)
            .containsExactlyInAnyOrderElementsOf(hackathon1.projectIds());

        assertThat(projects.stream().filter(p -> p.getId().equals(projectId2.value())).findFirst().orElseThrow().getOdHackStats())
            .usingRecursiveComparison()
            .isEqualTo(new ProjectShortResponseV2OdHackStats()
                    .issueCount(5)
                    .availableIssueCount(3));
    }

    @Test
    @Order(30)
    void should_get_hackathon_projects_with_available_issues() {
        // When
        final var projects = client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG_PROJECTS.formatted(hackathon1.slug()), Map.of("hasAvailableIssues", "true")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectPageResponseV2.class)
                .returnResult().getResponseBody().getProjects();

        assertThat(projects)
            .hasSize(1)
            .extracting(ProjectShortResponseV2::getId)
            .containsOnly(projectId2.value());
    }

    @Test
    @Order(30)
    void should_get_hackathon_events() {
        // When
        final var events = client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG_EVENTS.formatted(hackathon1.slug())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonEventsResponse.class)
                .returnResult().getResponseBody().getEvents();

        assertThat(events)
            .hasSize(4)
            .isSortedAccordingTo(Comparator.comparing(HackathonsEventItemResponse::getStartDate).reversed())
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoreAllExpectedNullFields(true)
                .build())
            .containsExactlyInAnyOrder(
                new HackathonsEventItemResponse()
                    .name("ODHack begins")
                    .subtitle("Get ready to start contributing, connecting & receiving rewards!")
                    .iconSlug("ri-calendar-line")
                    .links(List.of()),
                new HackathonsEventItemResponse()
                    .name("ODHack finishes")
                    .subtitle("All tasks should have been completed, now maintainers will review final work.")
                    .iconSlug("ri-calendar-line")
                    .links(List.of()),
                new HackathonsEventItemResponse()
                    .name("Event 1")
                    .subtitle("Event 1 is awesome")
                    .iconSlug("icon1")
                    .links(List.of(new SimpleLink().url("https://www.foo.bar").value("Foo"))),
                new HackathonsEventItemResponse()
                    .name("Event 2")
                    .subtitle("Event 2 is awesome")
                    .iconSlug("icon2")
                    .links(List.of(new SimpleLink().url("https://www.foo2.bar").value("Foo2")))
            );
    }


    @Test
    @Order(30)
    void should_filter_hackathon_events_by_date() {
        // Given
        final var midHackathon = hackathon1.startDate().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        {
            // When
            final var events = client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG_EVENTS.formatted(hackathon1.slug()), Map.of("fromDate", midHackathon)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonEventsResponse.class)
                .returnResult().getResponseBody().getEvents();

            assertThat(events)
                .hasSize(2)
                .isSortedAccordingTo(Comparator.comparing(HackathonsEventItemResponse::getStartDate).reversed())
                .extracting(HackathonsEventItemResponse::getName)
                .containsExactlyInAnyOrder("Event 2", "ODHack finishes");
        }


        {
            // When
            final var events = client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG_EVENTS.formatted(hackathon1.slug()), Map.of("toDate", midHackathon)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(HackathonEventsResponse.class)
                .returnResult().getResponseBody().getEvents();

            assertThat(events)
                .hasSize(3)
                .isSortedAccordingTo(Comparator.comparing(HackathonsEventItemResponse::getStartDate).reversed())
                .extracting(HackathonsEventItemResponse::getName)
                .containsExactlyInAnyOrder("Event 1", "ODHack begins", "Event 2");
        }
    }
}
