package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.GithubIssuePageResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.it.api.ApplicationsApiIT.fakeApplication;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ProjectGetPublicIssuesApiIT extends AbstractMarketplaceApiIT {
    private final static UUID CAL_DOT_COM = UUID.fromString("1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e");

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    HackathonStoragePort hackathonStoragePort;

    private UserAuthHelper.AuthenticatedUser antho;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAntho();
        final var pierre = userAuthHelper.authenticatePierre();
        final var ofux = userAuthHelper.authenticateOlivier();

        applicationRepository.saveAll(List.of(
                new ApplicationEntity(
                        UUID.fromString("f3706f53-bd79-4991-8a76-7dd12aef81dd"),
                        ZonedDateTime.of(2023, 11, 5, 9, 40, 41, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        pierre.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1980935024L,
                        1111L,
                        "I would like to work on this issue",
                        "I would do this and that"),
                new ApplicationEntity(
                        UUID.fromString("609231c0-b38c-4d5c-b21d-6307595f520f"),
                        ZonedDateTime.of(2023, 11, 7, 15, 26, 35, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        ofux.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1980935024L,
                        1112L,
                        "I am very interested!",
                        null),
                new ApplicationEntity(
                        UUID.fromString("bf6a909e-243d-4698-aa9f-5f40e3fb4826"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        antho.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1980935024L,
                        1113L,
                        "I could do it",
                        "No idea yet ¯\\_(ツ)_/¯"),
                new ApplicationEntity(
                        UUID.fromString("536532eb-ed7b-4461-884d-20e54ba9bec6"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        UUID.fromString("97f6b849-1545-4064-83f1-bc5ded33a8b3"),
                        antho.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1980935024L,
                        1113L,
                        "I could do it",
                        null),
                new ApplicationEntity(
                        UUID.fromString("90ec1b4e-4447-4997-aa9c-f25bb53a25d7"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        UUID.fromString(CAL_DOT_COM.toString()),
                        antho.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1978423500L,
                        1113L,
                        "I could do it",
                        null)
        ));


        final var startDate = ZonedDateTime.of(2024, 4, 19, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        final var hackathon = Hackathon.builder()
                .id(Hackathon.Id.of("e06aeec6-cec6-40e1-86cb-e741e0dacf25"))
                .title("Hackathon 1")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon.githubLabels().addAll(List.of("bug", "insights"));
        hackathon.projectIds().addAll(List.of(CAL_DOT_COM,
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                UUID.fromString("57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8")));
        hackathonStoragePort.save(hackathon);
    }

    @Test
    void should_get_applicant_count_and_issue_count_in_hackathon() {
        client.get()
                .uri(getApiURI(HACKATHONS_BY_SLUG.formatted("hackathon-1")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.subscriberCount").isEqualTo(0)
                .jsonPath("$.issueCount").isEqualTo(141)
                .jsonPath("$.openIssueCount").isEqualTo(5);

        client.get()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].subscriberCount").isEqualTo(0)
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].issueCount").isEqualTo(141)
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].openIssueCount").isEqualTo(5);

        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isAssigned", "false"
                )))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.totalItemNumber").isEqualTo(3);

        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted("57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8"), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isAssigned", "false"
                )))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.totalItemNumber").isEqualTo(2);

        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isApplied", "true"
                )))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.totalItemNumber").isEqualTo(1);
    }

    @Test
    void should_get_applied_issues_of_cal_dot_com_within_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isApplied", "true",
                        "direction", "DESC"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1978423500,
                              "number": 12239,
                              "title": "[CAL-2676] Average Event Duration in insights",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12239",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 53316345,
                                "login": "Udit-takkar",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-06T06:54:13Z",
                              "closedAt": null,
                              "body": "![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/64b12d24-13be-46e8-9024-085f4e96d1ef/040f427b-599f-4682-a371-e46d5044ba7d?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy82NGIxMmQyNC0xM2JlLTQ2ZTgtOTAyNC0wODVmNGU5NmQxZWYvMDQwZjQyN2ItNTk5Zi00NjgyLWEzNzEtZTQ2ZDUwNDRiYTdkIiwiaWF0IjoxNjk5MjUzNjYwLCJleHAiOjE2OTkzNDAwNjB9.NaDiPp6DzOKCgMXR_DUMbEyrfJrLwrn7ZKjVIb1hPqw)\\n\\nHow to reproduce ?\\n\\n1. Go to insights page\\n2. Select User Filter in filters and select any one user\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2676](https://linear.app/calcom/issue/CAL-2676/average-event-duration-in-insights)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDCC9 regressing",
                                  "description": "This used to work. Now it doesn't anymore."
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "OPEN",
                        "isApplied", "true"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 1
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isApplied", "true"
                )))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1978423500,
                              "number": 12239,
                              "title": "[CAL-2676] Average Event Duration in insights",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12239",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 53316345,
                                "login": "Udit-takkar",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-06T06:54:13Z",
                              "closedAt": null,
                              "body": "![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/64b12d24-13be-46e8-9024-085f4e96d1ef/040f427b-599f-4682-a371-e46d5044ba7d?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy82NGIxMmQyNC0xM2JlLTQ2ZTgtOTAyNC0wODVmNGU5NmQxZWYvMDQwZjQyN2ItNTk5Zi00NjgyLWEzNzEtZTQ2ZDUwNDRiYTdkIiwiaWF0IjoxNjk5MjUzNjYwLCJleHAiOjE2OTkzNDAwNjB9.NaDiPp6DzOKCgMXR_DUMbEyrfJrLwrn7ZKjVIb1hPqw)\\n\\nHow to reproduce ?\\n\\n1. Go to insights page\\n2. Select User Filter in filters and select any one user\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2676](https://linear.app/calcom/issue/CAL-2676/average-event-duration-in-insights)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDCC9 regressing",
                                  "description": "This used to work. Now it doesn't anymore."
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ],
                              "currentUserApplication": {
                                "id": "90ec1b4e-4447-4997-aa9c-f25bb53a25d7",
                                "applicant": {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true
                                },
                                "project": {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                },
                                "motivations": "I could do it",
                                "problemSolvingApproach": null
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_not_applied_issues_of_cal_dot_com_within_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isApplied", "false"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.totalItemNumber").isEqualTo(3)
                .jsonPath("$.issues[?(@.id == 1978423500)]").doesNotExist();

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "OPEN",
                        "isApplied", "false"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 3
                            },
                            {
                              "project": {
                                "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                "slug": "pizzeria-yoshi-",
                                "name": "Pizzeria Yoshi !",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                              },
                              "issueCount": 2
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues_of_cal_dot_com_within_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isGoodFirstIssue", "true"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1975516187,
                              "number": 12215,
                              "title": "[CAL-2671] Individual Insights Page has Team Insights",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12215",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 16177678,
                                "login": "shirazdole",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16177678?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-03T06:12:34Z",
                              "body": "Individual insights page has insights from from \\"most booked\\" and \\"least booked\\" members. It should just be data that pretains to them:\\n\\n![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/e74bdb02-5e26-41e8-87be-8dda7e382fd4/f48ddf61-24ba-4a09-b94c-ed1aa2b50df4?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy9lNzRiZGIwMi01ZTI2LTQxZTgtODdiZS04ZGRhN2UzODJmZDQvZjQ4ZGRmNjEtMjRiYS00YTA5LWI5NGMtZWQxYWEyYjUwZGY0IiwiaWF0IjoxNjk4OTkxOTYyLCJleHAiOjE2OTkwNzgzNjJ9.dSRqQBHSC2TupIr9T7wnYSh8Vm3x1KlTsSEo1NIejwI)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2671](https://linear.app/calcom/issue/CAL-2671/individual-insights-page-has-team-insights)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "good first issue",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "OPEN",
                        "isGoodFirstIssue", "true"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 1
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues_of_cal_dot_com_that_are_not_within_any_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "isIncludedInAnyHackathon", "false",
                        "isGoodFirstIssue", "true"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.totalItemNumber").isEqualTo(15)
                .jsonPath("$.issues[?(@.id == 1975516187)]").doesNotExist();
    }

    @Test
    void should_get_open_rust_and_go_issues_of_cal_dot_com_within_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "statuses", "OPEN",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "languageIds", "ca600cac-0f45-44e9-a6e8-25e21b0c6887,c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                        "direction", "DESC"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1978423500,
                              "number": 12239,
                              "title": "[CAL-2676] Average Event Duration in insights",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12239",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 53316345,
                                "login": "Udit-takkar",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-06T06:54:13Z",
                              "closedAt": null,
                              "body": "![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/64b12d24-13be-46e8-9024-085f4e96d1ef/040f427b-599f-4682-a371-e46d5044ba7d?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy82NGIxMmQyNC0xM2JlLTQ2ZTgtOTAyNC0wODVmNGU5NmQxZWYvMDQwZjQyN2ItNTk5Zi00NjgyLWEzNzEtZTQ2ZDUwNDRiYTdkIiwiaWF0IjoxNjk5MjUzNjYwLCJleHAiOjE2OTkzNDAwNjB9.NaDiPp6DzOKCgMXR_DUMbEyrfJrLwrn7ZKjVIb1hPqw)\\n\\nHow to reproduce ?\\n\\n1. Go to insights page\\n2. Select User Filter in filters and select any one user\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2676](https://linear.app/calcom/issue/CAL-2676/average-event-duration-in-insights)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDCC9 regressing",
                                  "description": "This used to work. Now it doesn't anymore."
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1975516187,
                              "number": 12215,
                              "title": "[CAL-2671] Individual Insights Page has Team Insights",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12215",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 16177678,
                                "login": "shirazdole",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16177678?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-03T06:12:34Z",
                              "closedAt": null,
                              "body": "Individual insights page has insights from from \\"most booked\\" and \\"least booked\\" members. It should just be data that pretains to them:\\n\\n![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/e74bdb02-5e26-41e8-87be-8dda7e382fd4/f48ddf61-24ba-4a09-b94c-ed1aa2b50df4?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy9lNzRiZGIwMi01ZTI2LTQxZTgtODdiZS04ZGRhN2UzODJmZDQvZjQ4ZGRmNjEtMjRiYS00YTA5LWI5NGMtZWQxYWEyYjUwZGY0IiwiaWF0IjoxNjk4OTkxOTYyLCJleHAiOjE2OTkwNzgzNjJ9.dSRqQBHSC2TupIr9T7wnYSh8Vm3x1KlTsSEo1NIejwI)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2671](https://linear.app/calcom/issue/CAL-2671/individual-insights-page-has-team-insights)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "good first issue",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1849984260,
                              "number": 10746,
                              "title": "[CAL-2346] /insights: show ALL booking data of teams and Orgs",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/10746",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 8019099,
                                "login": "PeerRich",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-08-14T15:10:16Z",
                              "closedAt": null,
                              "body": "ideally we also have a table view with all bookings from teams and orgs.\\r\\n\\r\\nonly owners and admins should see this\\n\\n<sub>[CAL-2346](https://linear.app/calcom/issue/CAL-2346/insights-show-all-booking-data-of-teams-and-orgs)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "teams",
                                  "description": "area: teams, round robin, collective, managed event-types"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1787557848,
                              "number": 9911,
                              "title": "Feature: see previous attendees of event-types",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/9911",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 8019099,
                                "login": "PeerRich",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-07-04T09:38:55Z",
                              "closedAt": null,
                              "body": "not sure if this would live inside /insights or in /event-types/ID but it would be dope to see a table of previous attendees",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "event-types",
                                  "description": "area: event types, event-types"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "OPEN",
                        "languageIds", "ca600cac-0f45-44e9-a6e8-25e21b0c6887,c83881b3-5aef-4819-9596-fdbbbedf2b0b"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 4
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_completed_issues_of_cal_dot_com_within_hackathon() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "sort", "CLOSED_AT",
                        "direction", "DESC",
                        "statuses", "COMPLETED",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 7,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1849986822,
                              "number": 10747,
                              "title": "[CAL-2347] /insights: be able to access raw data via csv api endpoint",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/10747",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 8019099,
                                "login": "PeerRich",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-08-14T15:11:54Z",
                              "closedAt": "2023-10-16T11:27:27Z",
                              "body": "same as metabase, it would be great if you can copy the link to the real-time raw data as a csv:\\n\\n![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/2d88ae81-840a-4067-b3bc-5a5cde4ae6a7/b7afb9d4-b2f6-43e8-bb6b-25fb06c136a4?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy8yZDg4YWU4MS04NDBhLTQwNjctYjNiYy01YTVjZGU0YWU2YTcvYjdhZmI5ZDQtYjJmNi00M2U4LWJiNmItMjVmYjA2YzEzNmE0IiwiaWF0IjoxNjkyMDI1OTIwLCJleHAiOjE2OTIxMTIzMjB9.92VOOK1GJQRZlOu11gw65GUcX6ntT1OxSG_elgcMelY)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2347](https://linear.app/calcom/issue/CAL-2347/insights-be-able-to-access-raw-data-via-csv-api-endpoint)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83D\\uDEA7  wip / in the making",
                                  "description": "This is currently being worked on"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 2538462,
                                  "login": "keithwillcode",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/2538462?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1829562256,
                              "number": 10486,
                              "title": "Several bugs in insights",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/10486",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 81948346,
                                "login": "anikdhabal",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/81948346?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-07-31T16:00:31Z",
                              "closedAt": "2023-10-04T19:10:11Z",
                              "body": "Found a bug? Please fill out the sections below. \\uD83D\\uDC4D\\r\\n\\r\\n### Issue Summary\\r\\n\\r\\nI found bugs in the insights section. Today, I created two team meetings: one was scheduled, and the other was canceled. However, the insights section is showing that 2 events were created and 0 events were canceled.\\r\\n![Screenshot 2023-07-31 212150](https://github.com/calcom/cal.com/assets/81948346/892f89ed-9bdd-464c-aad7-828da5236ec7)\\r\\n\\r\\nToday is the 31st, but I noticed that the trace graph's end date varies when using different filters.\\r\\n![Screenshot 2023-07-31 211352](https://github.com/calcom/cal.com/assets/81948346/6e7fb7da-9afd-452f-92c7-ef7cd71454dc)\\r\\n\\r\\n![Screenshot 2023-07-31 211003](https://github.com/calcom/cal.com/assets/81948346/383e4a55-7590-44af-bd52-490d3f6b878e)\\r\\n\\r\\n![Screenshot 2023-07-31 210644](https://github.com/calcom/cal.com/assets/81948346/7d4e8f2e-c99b-4503-b6dc-615d9a88707d)\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n### Steps to Reproduce\\r\\n\\r\\n1. Go to insights\\r\\n\\r\\n",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                },
                                {
                                  "name": "\\uD83D\\uDC69‍\\uD83D\\uDD2C needs investigation",
                                  "description": "Needs to be investigated further"
                                },
                                {
                                  "name": "\\uD83D\\uDEA7  wip / in the making",
                                  "description": "This is currently being worked on"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 4461358,
                                  "login": "alannnc",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1895586558,
                              "number": 11352,
                              "title": "[CAL-2466] Insights re renders and loads every endpoint again, possible bug with state changing when it shouldn't",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11352",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 4461358,
                                "login": "alannnc",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-09-14T03:20:34Z",
                              "closedAt": "2023-09-18T23:36:10Z",
                              "body": "\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2466](https://linear.app/calcom/issue/CAL-2466/insights-re-renders-and-loads-every-endpoint-again-possible-bug-with)</sub>",
                              "labels": [
                                {
                                  "name": "High priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                },
                                {
                                  "name": "\\uD83D\\uDEA7  wip / in the making",
                                  "description": "This is currently being worked on"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 4461358,
                                  "login": "alannnc",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1877889025,
                              "number": 11087,
                              "title": "[CAL-2401] Teams are listed on insights but then you can't find them on settings page.",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11087",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 4461358,
                                "login": "alannnc",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-09-01T18:40:43Z",
                              "closedAt": "2023-09-04T15:47:56Z",
                              "body": "In an org enviroment this happened recently. Let's figure out where they should be listed on the settings page.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2401](https://linear.app/calcom/issue/CAL-2401/teams-are-listed-on-insights-but-then-you-cant-find-them-on-settings)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "organizations",
                                  "description": "area: organizations, orgs"
                                },
                                {
                                  "name": "teams",
                                  "description": "area: teams, round robin, collective, managed event-types"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                },
                                {
                                  "name": "\\uD83D\\uDEA7  wip / in the making",
                                  "description": "This is currently being worked on"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 4461358,
                                  "login": "alannnc",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1799417431,
                              "number": 10068,
                              "title": "[CAL-2147] Insights getTotalRescheduledEvents goes wrong for large booking counts",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/10068",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1046695,
                                "login": "emrysal",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-07-11T17:28:42Z",
                              "closedAt": "2023-08-10T17:36:31Z",
                              "body": "Insights getTotalRescheduledEvents goes wrong for large booking counts. - probably same for getTotalCanceledEvents. Looks like we crash when there's a large amount of bookings due to a limit of 32767 on the Prisma `in` clause. \\r\\n\\r\\n \\n\\n<sub>[CAL-2147](https://linear.app/calcom/issue/CAL-2147/insights-gettotalrescheduledevents-goes-wrong-for-large-booking-counts)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 4461358,
                                  "login": "alannnc",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4461358?v=4"
                                }
                              ],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "COMPLETED"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 7
                            },
                            {
                              "project": {
                                "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                "slug": "pizzeria-yoshi-",
                                "name": "Pizzeria Yoshi !",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                              },
                              "issueCount": 73
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_search_by_title_assigned_issues_of_cal_dot_com() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "isAssigned", "true",
                        "search", "CAL-1713",
                        "direction", "DESC"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 2014719100,
                              "number": 12571,
                              "title": "[CAL-1713] Ability to create one-off meetings",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12571",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "owner": "calcom",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 8019099,
                                "login": "PeerRich",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-28T15:33:05Z",
                              "body": "The ability to quickly create one-off appointments with custom titles, locations, descriptions, time slots, etc. would be awesome.\\n\\nFeel free to check out Calendly for inspiration. But it would be great if you could develop a more sophisticated solution than the one from Calendly. :)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-1713](https://linear.app/calcom/issue/CAL-1713/ability-to-create-one-off-meetings)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "bookings",
                                  "description": "area: bookings, availability, timezones, double booking"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1615602441,
                              "number": 7595,
                              "title": "[CAL-1713] Ability to create one-off meetings",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/7595",
                              "repo": {
                                "id": 350360184,
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 126205884,
                                "login": "RainerZufall9393",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/126205884?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-03-08T16:58:45Z",
                              "body": "The ability to quickly create one-off appointments with custom titles, locations, descriptions, time slots, etc. would be awesome.\\r\\n\\r\\nFeel free to check out Calendly for inspiration. But it would be great if you could develop a more sophisticated solution than the one from Calendly. :)\\n\\n<sub>[CAL-1713](https://linear.app/calcom/issue/CAL-1713/ability-to-create-one-off-meetings)</sub>",
                              "labels": [
                                {
                                  "name": "3 points",
                                  "description": "Created by SyncLinear.com"
                                },
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83D\\uDE4B\\uD83C\\uDFFB‍♂️help wanted",
                                  "description": "Help from the community is appreciated"
                                },
                                {
                                  "name": "\\uD83D\\uDEA8 needs approval",
                                  "description": "This feature request has not been reviewed yet by the Product Team and needs approval beforehand"
                                }
                              ],
                              "applicants": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "isAssigned", "true",
                        "search", "counting managed event"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "languages": [
                            {
                              "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                              "slug": "javascript",
                              "name": "Javascript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                            },
                            {
                              "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                              "slug": "typescript",
                              "name": "Typescript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                            },
                            {
                              "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                              "slug": "python",
                              "name": "Python",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                            }
                          ],
                          "projects": [
                            {
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "issueCount": 1
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "direction", "DESC"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 10,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1980935024,
                              "number": 12255,
                              "title": "[CAL-2679] Nice find. Unit testable (could be follow up)?",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12255",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-07T09:40:41Z",
                              "closedAt": null,
                              "body": "> Nice find. Unit testable (could be follow up)?\\n\\n\\\\*Originally posted by @keithwillcode in \\\\*[*https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626*](https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626)\\n\\nWrite unit/integration tests for defaultResponder and defaultHandler that can ensure that it doesn't add the header again if already added.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2679](https://linear.app/calcom/issue/CAL-2679/nice-find-unit-testable-could-be-follow-up)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "api",
                                  "description": "area: API, enterprise API, access token, OAuth"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                                }
                              ],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1980731400,
                              "number": 12253,
                              "title": "[CAL-2678] No loader on clicking install button from apps listing page.",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12253",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-07T07:36:17Z",
                              "closedAt": null,
                              "body": "https://www.loom.com/share/f934ebd75f0343928715860d7ec39787\\n\\n<sub>[CAL-2678](https://linear.app/calcom/issue/CAL-2678/no-loader-on-clicking-install-button-from-apps-listing-page)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1979376167,
                              "number": 12244,
                              "title": "Read skippable events to calculate realistic availability",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12244",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 467258,
                                "login": "leog",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/467258?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-06T15:01:09Z",
                              "closedAt": null,
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWhen connecting packed calendars, there can be little room for availability. Being able to distinguish events that can be marked with a different color or events with optional rsvp to be skippable, may help users with little availability to better accommodate new meetings through Cal.\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAs mentioned, having the chance to read rsvp from events to calculate availability can help free up more slots for new meetings.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1943730312,
                              "number": 11900,
                              "title": "Conditional Questions on Event Types",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11900",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 10899980,
                                "login": "moilejter",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/10899980?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-10-15T05:42:44Z",
                              "closedAt": null,
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWe would like to have the ability to ask \\"follow-up questions\\" based on the answers to previous questions in the booking request.  For example, we could have a mandatory question that asks \\"how did you hear about us?\\" with possible answers \\"web\\", \\"print\\", \\"social media\\", \\"personal reference\\" - and If they choose \\"web\\", we'd like to ask \\"source web site\\" (a text field), or if they choose \\"social media\\", we'd like to ask \\"social media site\\" (a drop down with entries for X/tweeter, Mastodon, Facebook, other).\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAdd an option question type, which in addition to the existing question template, asks for the parent question identifier, and a value (or a list of values) that would make this question appear below the parent question.\\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\nWe haven't come up with an alternative we liked.\\r\\n\\r\\n### Additional context\\r\\n\\r\\nDid not find this already suggested in either \\"Issues\\" or \\"Pull Requests\\"\\r\\n\\r\\n### Requirement/Document\\r\\n\\r\\nWe haven't written one up.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "booking-page",
                                  "description": "area: booking page, public booking page, booker"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1930038179,
                              "number": 11732,
                              "title": "organization - org owners and admins should show their respective badge when they join a sub-team",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11732",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 32706411,
                                "login": "SomayChauhan",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/32706411?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-10-06T12:10:09Z",
                              "closedAt": null,
                              "body": "## Issue Summary\\r\\n### this is my organization members page\\r\\n![Screenshot from 2023-10-06 17-17-07](https://github.com/calcom/cal.com/assets/32706411/d1f1aad3-2947-4467-be21-e726430874a1)\\r\\n* as you can see i have temp1 as admin and somay and somay2  as owners\\r\\n### this is one the teams under my organizations\\r\\n![Screenshot from 2023-10-06 17-22-20](https://github.com/calcom/cal.com/assets/32706411/ac148a94-56c7-405b-977c-02fa3b495c5b)\\r\\n* as you can see all of them are joined as members but temp1, somay and somay2 all have the admin access to these teams i.e. they can add and remove members from teams so they should show owner/admin instead of member\\r\\n\\r\\nPS: this is just a suggestion however, I may be wrong here.",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "organizations",
                                  "description": "area: organizations, orgs"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues_as_registered_user() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "direction", "DESC"
                )))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 10,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1980935024,
                              "number": 12255,
                              "title": "[CAL-2679] Nice find. Unit testable (could be follow up)?",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12255",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-07T09:40:41Z",
                              "closedAt": null,
                              "body": "> Nice find. Unit testable (could be follow up)?\\n\\n\\\\*Originally posted by @keithwillcode in \\\\*[*https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626*](https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626)\\n\\nWrite unit/integration tests for defaultResponder and defaultHandler that can ensure that it doesn't add the header again if already added.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2679](https://linear.app/calcom/issue/CAL-2679/nice-find-unit-testable-could-be-follow-up)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "api",
                                  "description": "area: API, enterprise API, access token, OAuth"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                                }
                              ],
                              "assignees": [],
                              "currentUserApplication": {
                                "id": "bf6a909e-243d-4698-aa9f-5f40e3fb4826",
                                "applicant": {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true
                                },
                                "project": {
                                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                  "slug": "calcom",
                                  "name": "Cal.com",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                                },
                                "motivations": "I could do it",
                                "problemSolvingApproach": "No idea yet ¯\\\\_(ツ)_/¯"
                              }
                            },
                            {
                              "id": 1980731400,
                              "number": 12253,
                              "title": "[CAL-2678] No loader on clicking install button from apps listing page.",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12253",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-07T07:36:17Z",
                              "closedAt": null,
                              "body": "https://www.loom.com/share/f934ebd75f0343928715860d7ec39787\\n\\n<sub>[CAL-2678](https://linear.app/calcom/issue/CAL-2678/no-loader-on-clicking-install-button-from-apps-listing-page)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1979376167,
                              "number": 12244,
                              "title": "Read skippable events to calculate realistic availability",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12244",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 467258,
                                "login": "leog",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/467258?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-11-06T15:01:09Z",
                              "closedAt": null,
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWhen connecting packed calendars, there can be little room for availability. Being able to distinguish events that can be marked with a different color or events with optional rsvp to be skippable, may help users with little availability to better accommodate new meetings through Cal.\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAs mentioned, having the chance to read rsvp from events to calculate availability can help free up more slots for new meetings.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1943730312,
                              "number": 11900,
                              "title": "Conditional Questions on Event Types",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11900",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 10899980,
                                "login": "moilejter",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/10899980?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-10-15T05:42:44Z",
                              "closedAt": null,
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWe would like to have the ability to ask \\"follow-up questions\\" based on the answers to previous questions in the booking request.  For example, we could have a mandatory question that asks \\"how did you hear about us?\\" with possible answers \\"web\\", \\"print\\", \\"social media\\", \\"personal reference\\" - and If they choose \\"web\\", we'd like to ask \\"source web site\\" (a text field), or if they choose \\"social media\\", we'd like to ask \\"social media site\\" (a drop down with entries for X/tweeter, Mastodon, Facebook, other).\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAdd an option question type, which in addition to the existing question template, asks for the parent question identifier, and a value (or a list of values) that would make this question appear below the parent question.\\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\nWe haven't come up with an alternative we liked.\\r\\n\\r\\n### Additional context\\r\\n\\r\\nDid not find this already suggested in either \\"Issues\\" or \\"Pull Requests\\"\\r\\n\\r\\n### Requirement/Document\\r\\n\\r\\nWe haven't written one up.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "booking-page",
                                  "description": "area: booking page, public booking page, booker"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1930038179,
                              "number": 11732,
                              "title": "organization - org owners and admins should show their respective badge when they join a sub-team",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11732",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 32706411,
                                "login": "SomayChauhan",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/32706411?v=4",
                                "isRegistered": false
                              },
                              "createdAt": "2023-10-06T12:10:09Z",
                              "closedAt": null,
                              "body": "## Issue Summary\\r\\n### this is my organization members page\\r\\n![Screenshot from 2023-10-06 17-17-07](https://github.com/calcom/cal.com/assets/32706411/d1f1aad3-2947-4467-be21-e726430874a1)\\r\\n* as you can see i have temp1 as admin and somay and somay2  as owners\\r\\n### this is one the teams under my organizations\\r\\n![Screenshot from 2023-10-06 17-22-20](https://github.com/calcom/cal.com/assets/32706411/ac148a94-56c7-405b-977c-02fa3b495c5b)\\r\\n* as you can see all of them are joined as members but temp1, somay and somay2 all have the admin access to these teams i.e. they can add and remove members from teams so they should show owner/admin instead of member\\r\\n\\r\\nPS: this is just a suggestion however, I may be wrong here.",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "organizations",
                                  "description": "area: organizations, orgs"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        
                        """);
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class ProjectsGetIssuesApiIT {

        final UUID projectAppliedTo1 = UUID.fromString("27ca7e18-9e71-468f-8825-c64fe6b79d66");
        final UUID projectAppliedTo2 = UUID.fromString("57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8");

        @Test
        @Order(0)
        void setupOnce() {
            final var pierre = userAuthHelper.authenticatePierre();
            final var antho = userAuthHelper.authenticateAntho();
            final var olivier = userAuthHelper.authenticateOlivier();

            applicationRepository.saveAll(List.of(
                    // 1652216316L has 2 applicants on project 2
                    // 1652216317L has 2 applicants on project 1 and 1 applicant on project 2
                    fakeApplication(projectAppliedTo1, pierre, 1651834617L, 112L),
                    fakeApplication(projectAppliedTo2, pierre, 1652216316L, 113L),

                    fakeApplication(projectAppliedTo2, antho, 1652216316L, 112L),
                    fakeApplication(projectAppliedTo2, antho, 1651834617L, 113L),

                    fakeApplication(projectAppliedTo1, olivier, 1651834617L, 112L)
            ));
        }

        @Test
        @Order(1)
        void should_return_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "3")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .json("""
                            {
                              "totalPageNumber": 564,
                              "totalItemNumber": 1691,
                              "hasMore": true,
                              "nextPageIndex": 1,
                              "issues": [
                                {
                                  "id": 1564131775,
                                  "number": 1,
                                  "title": "testing",
                                  "status": "OPEN",
                                  "htmlUrl": "https://github.com/gregcha/crew-app/issues/1",
                                  "repo": {
                                    "id": 302082426,
                                    "owner": "gregcha",
                                    "name": "crew-app",
                                    "description": null,
                                    "htmlUrl": "https://github.com/gregcha/crew-app"
                                  },
                                  "author": {
                                    "githubUserId": 8642470,
                                    "login": "gregcha",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                    "isRegistered": true
                                  },
                                  "createdAt": "2023-01-31T11:26:54Z",
                                  "closedAt": null,
                                  "body": "testing issues",
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": [],
                                  "currentUserApplication": null
                                },
                                {
                                  "id": 1642022365,
                                  "number": 6,
                                  "title": "This is a new issue",
                                  "status": "COMPLETED",
                                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/6",
                                  "repo": {
                                    "id": 602953043,
                                    "owner": "od-mocks",
                                    "name": "cool-repo-A",
                                    "description": "This is repo A for our e2e tests",
                                    "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                                  },
                                  "author": {
                                    "githubUserId": 43467246,
                                    "login": "AnthonyBuisset",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                    "isRegistered": true
                                  },
                                  "createdAt": "2023-03-27T12:56:04Z",
                                  "closedAt": "2023-08-30T09:20:48Z",
                                  "body": null,
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": [
                                    {
                                      "githubUserId": 43467246,
                                      "login": "AnthonyBuisset",
                                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                    }
                                  ],
                                  "currentUserApplication": null
                                },
                                {
                                  "id": 1642022454,
                                  "number": 7,
                                  "title": "This one has been cancelled",
                                  "status": "CANCELLED",
                                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/7",
                                  "repo": {
                                    "id": 602953043,
                                    "owner": "od-mocks",
                                    "name": "cool-repo-A",
                                    "description": "This is repo A for our e2e tests",
                                    "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                                  },
                                  "author": {
                                    "githubUserId": 43467246,
                                    "login": "AnthonyBuisset",
                                    "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                    "isRegistered": true
                                  },
                                  "createdAt": "2023-03-27T12:56:32Z",
                                  "closedAt": "2023-03-27T12:57:25Z",
                                  "body": null,
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": [],
                                  "currentUserApplication": null
                                }
                              ]
                            }
                            """);
        }

        @Test
        @Order(1)
        void should_return_assigned_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            final var issues = client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "true")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

            assertThat(issues).isNotEmpty();
            issues.forEach(issue -> assertThat(issue.getAssignees()).isNotEmpty());
        }

        @Test
        @Order(1)
        void should_return_unassigned_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            final var issues = client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "false")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

            assertThat(issues).isNotEmpty();
            issues.forEach(issue -> assertThat(issue.getAssignees()).isEmpty());
        }

        @Test
        @Order(1)
        void should_return_unassigned_open_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            final var issues = client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of(
                            "pageIndex", "0",
                            "pageSize", "30",
                            "isAssigned", "false",
                            "statuses", "OPEN")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

            assertThat(issues).isNotEmpty();
            assertThat(issues).allMatch(issue -> issue.getAssignees().isEmpty() && issue.getStatus() == GithubIssueStatus.OPEN);
        }

        @Test
        @Order(1)
        void should_return_applied_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            final var issues = client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "true")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

            assertThat(issues).isNotEmpty();
            issues.forEach(issue -> assertThat(issue.getApplicants()).isNotEmpty());
        }

        @Test
        @Order(1)
        void should_return_not_applied_project_issues() {
            // Given
            final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

            final var issues = client.get()
                    .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "false")))
                    .header("Authorization", BEARER_PREFIX + jwt)
                    // Then
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

            assertThat(issues).isNotEmpty();
            issues.forEach(issue -> assertThat(issue.getApplicants()).isEmpty());
        }

    }
}
