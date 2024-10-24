package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.GithubIssuePageResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.helper.DatabaseHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
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
    @Autowired
    PostgresBiProjectorAdapter biProjectorAdapter;
    @Autowired
    DatabaseHelper databaseHelper;

    private UserAuthHelper.AuthenticatedUser antho;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAntho();
        final var pierre = userAuthHelper.authenticatePierre();
        final var ofux = userAuthHelper.authenticateOlivier();

        final var applications = List.of(
                new ApplicationEntity(
                        UUID.fromString("f3706f53-bd79-4991-8a76-7dd12aef81dd"),
                        ZonedDateTime.of(2023, 11, 5, 9, 40, 41, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        pierre.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1998815347L,
                        1111L,
                        "I would like to work on this issue"),
                new ApplicationEntity(
                        UUID.fromString("609231c0-b38c-4d5c-b21d-6307595f520f"),
                        ZonedDateTime.of(2023, 11, 7, 15, 26, 35, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        ofux.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1998815347L,
                        1112L,
                        "I am very interested!",
                        null),
                new ApplicationEntity(
                        UUID.fromString("bf6a909e-243d-4698-aa9f-5f40e3fb4826"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        antho.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1998815347L,
                        1113L,
                        "I could do it"),
                new ApplicationEntity(
                        UUID.fromString("536532eb-ed7b-4461-884d-20e54ba9bec6"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        UUID.fromString("97f6b849-1545-4064-83f1-bc5ded33a8b3"),
                        antho.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1998815347L,
                        1113L,
                        "I could do it",
                        null),
                new ApplicationEntity(
                        UUID.fromString("90ec1b4e-4447-4997-aa9c-f25bb53a25d7"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        UUID.fromString(CAL_DOT_COM.toString()),
                        antho.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        2012872003L,
                        1113L,
                        "I could do it",
                        null)
        );
        databaseHelper.executeInTransaction(() -> {
            applicationRepository.saveAll(applications);
            applications.forEach(a -> biProjectorAdapter.onApplicationCreated(new Application(
                    Application.Id.of(a.id()),
                    ProjectId.of(a.projectId()),
                    a.applicantId(),
                    a.origin(),
                    a.receivedAt(),
                    GithubIssue.Id.of(a.issueId()),
                    GithubComment.Id.of(a.commentId()),
                    a.commentBody()
            )));
        });


        final var startDate = ZonedDateTime.of(2024, 4, 19, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        final var hackathon = Hackathon.builder()
                .id(Hackathon.Id.of("e06aeec6-cec6-40e1-86cb-e741e0dacf25"))
                .title("Hackathon 1")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .status(Hackathon.Status.PUBLISHED)
                .build();
        hackathon.githubLabels().addAll(List.of("High priority", "Medium priority", "Low priority"));
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
                .jsonPath("$.issueCount").isEqualTo(45)
                .jsonPath("$.openIssueCount").isEqualTo(28);

        client.get()
                .uri(getApiURI(HACKATHONS))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].subscriberCount").isEqualTo(0)
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].issueCount").isEqualTo(45)
                .jsonPath("$.hackathons[?(@.slug == 'hackathon-1')].openIssueCount").isEqualTo(28);

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
                .jsonPath("$.totalItemNumber").isEqualTo(28);

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
                .jsonPath("$.totalItemNumber").isEqualTo(0);

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
                .jsonPath("$.totalItemNumber").isEqualTo(2);
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
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 2012872003,
                              "number": 12562,
                              "title": "[CAL-2768]  Integration of Sentry and/or New Relic for Clean Performance Tracing",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12562",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 2538462,
                                "login": "keithwillcode",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/2538462?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-27T18:39:07Z",
                              "closedAt": null,
                              "body": "To enhance our application's performance monitoring capabilities, we aim to integrate Sentry and/or New Relic seamlessly into our codebase. The challenge is to achieve this integration in a clean and efficient manner, avoiding code clutter while ensuring comprehensive performance tracing.\\n\\n### Desired Features\\n\\n* **Sentry/New Relic Integration**: Integrate Sentry and/or New Relic for performance monitoring, tracing, and insights into application bottlenecks.\\n* **Minimal Code Impact**: Implement the integrations with minimal impact on the existing codebase, avoiding unnecessary clutter. Ideally we don't litter the code with tracing statements.\\n\\n### Requirements\\n\\n* **Documentation**: Create clear and concise documentation for developers, outlining the steps for integrating Sentry and/or New Relic and any additional configuration options.\\n* **Testing**: Perform thorough testing to ensure that the integrations work as expected without negatively impacting application performance.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2768](https://linear.app/calcom/issue/CAL-2768/integration-of-sentry-andor-new-relic-for-clean-performance-tracing)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "foundation",
                                  "description": ""
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "performance",
                                  "description": "area: performance, page load, slow, slow endpoints, loading screen, unresponsive"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83E\\uDDF9 Improvements",
                                  "description": "Improvements to existing features. Mostly UX/UI"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "90ec1b4e-4447-4997-aa9c-f25bb53a25d7"
                                }
                              ],
                              "assignees": []
                            },
                            {
                              "id": 1998815347,
                              "number": 12407,
                              "title": "[CAL-2735] Issue with Email Notifications",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12407",
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
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-17T10:46:50Z",
                              "closedAt": null,
                              "body": "https://www.loom.com/share/268a3400267340349da2e175fea96194?sid=ba761580-c88a-4957-91d4-2bcedd0d0d79\\n\\n<sub>[CAL-2735](https://linear.app/calcom/issue/CAL-2735/issue-with-email-notifications)</sub>",
                              "labels": [
                                {
                                  "name": "High priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "emails",
                                  "description": "area: emails, cancellation email, reschedule email, inbox, spam folder, not getting email"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "536532eb-ed7b-4461-884d-20e54ba9bec6"
                                },
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "bf6a909e-243d-4698-aa9f-5f40e3fb4826"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "isRegistered": true,
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0",
                                  "applicationId": "f3706f53-bd79-4991-8a76-7dd12aef81dd"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                  "isRegistered": true,
                                  "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                                  "applicationId": "609231c0-b38c-4d5c-b21d-6307595f520f"
                                }
                              ],
                              "assignees": []
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
                              "issueCount": 2
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
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1998815347,
                              "number": 12407,
                              "title": "[CAL-2735] Issue with Email Notifications",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12407",
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
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-17T10:46:50Z",
                              "closedAt": null,
                              "body": "https://www.loom.com/share/268a3400267340349da2e175fea96194?sid=ba761580-c88a-4957-91d4-2bcedd0d0d79\\n\\n<sub>[CAL-2735](https://linear.app/calcom/issue/CAL-2735/issue-with-email-notifications)</sub>",
                              "labels": [
                                {
                                  "name": "High priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "Stale",
                                  "description": null
                                },
                                {
                                  "name": "emails",
                                  "description": "area: emails, cancellation email, reschedule email, inbox, spam folder, not getting email"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "isRegistered": true,
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0",
                                  "applicationId": "f3706f53-bd79-4991-8a76-7dd12aef81dd"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                  "isRegistered": true,
                                  "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                                  "applicationId": "609231c0-b38c-4d5c-b21d-6307595f520f"
                                },
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "bf6a909e-243d-4698-aa9f-5f40e3fb4826"
                                },
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "536532eb-ed7b-4461-884d-20e54ba9bec6"
                                }
                              ],
                              "assignees": []
                            },
                            {
                              "id": 2012872003,
                              "number": 12562,
                              "title": "[CAL-2768]  Integration of Sentry and/or New Relic for Clean Performance Tracing",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12562",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 2538462,
                                "login": "keithwillcode",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/2538462?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-27T18:39:07Z",
                              "closedAt": null,
                              "body": "To enhance our application's performance monitoring capabilities, we aim to integrate Sentry and/or New Relic seamlessly into our codebase. The challenge is to achieve this integration in a clean and efficient manner, avoiding code clutter while ensuring comprehensive performance tracing.\\n\\n### Desired Features\\n\\n* **Sentry/New Relic Integration**: Integrate Sentry and/or New Relic for performance monitoring, tracing, and insights into application bottlenecks.\\n* **Minimal Code Impact**: Implement the integrations with minimal impact on the existing codebase, avoiding unnecessary clutter. Ideally we don't litter the code with tracing statements.\\n\\n### Requirements\\n\\n* **Documentation**: Create clear and concise documentation for developers, outlining the steps for integrating Sentry and/or New Relic and any additional configuration options.\\n* **Testing**: Perform thorough testing to ensure that the integrations work as expected without negatively impacting application performance.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2768](https://linear.app/calcom/issue/CAL-2768/integration-of-sentry-andor-new-relic-for-clean-performance-tracing)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "foundation",
                                  "description": ""
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "performance",
                                  "description": "area: performance, page load, slow, slow endpoints, loading screen, unresponsive"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83E\\uDDF9 Improvements",
                                  "description": "Improvements to existing features. Mostly UX/UI"
                                }
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                  "applicationId": "90ec1b4e-4447-4997-aa9c-f25bb53a25d7"
                                }
                              ],
                              "assignees": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_available_issues_of_cal_dot_com_within_hackathon() {
        // When
        final var response = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isAvailable", "true"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getIssues()).hasSize(5);
        assertThat(response.getIssues()).allMatch(i -> i.getStatus() == GithubIssueStatus.OPEN && i.getAssignees().isEmpty());

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "isAvailable", "true"
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
                                "name": "Cal.com"
                              },
                              "issueCount": 28
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_unavailable_issues_of_cal_dot_com_within_hackathon() {
        // When
        final var response = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5",
                        "hackathonId", "e06aeec6-cec6-40e1-86cb-e741e0dacf25",
                        "isAvailable", "false"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getTotalItemNumber()).isEqualTo(17);
        assertThat(response.getIssues()).allMatch(i -> i.getStatus() != GithubIssueStatus.OPEN || !i.getAssignees().isEmpty());

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "isAvailable", "false"
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
                                "name": "Cal.com"
                              },
                              "issueCount": 17
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
                .jsonPath("$.totalItemNumber").isEqualTo(31)
                .jsonPath("$.issues[?(@.id == 2012872003)]").doesNotExist();

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
                          "languages": [
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
                              "issueCount": 31
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
                          "totalItemNumber": 5,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1265009094,
                              "number": 3026,
                              "title": "It's not yet possible to deploy Cal on Cloudron",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/3026",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 2113743,
                                "login": "jdaviescoates",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/2113743?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-06-08T16:26:05Z",
                              "closedAt": null,
                              "body": "I've been using the hosted version and really like it, but I'd prefer to self-host and so I'd really love [Cloudron](https://cloudron.io/?refcode=5adcafc820c53c3d) to be added here:\\r\\n\\r\\n![Cal com_deployment](https://user-images.githubusercontent.com/2113743/172667667-07b5d14c-5af2-4675-a409-815302a34295.png)\\r\\n\\r\\nAs would lots of other Cloudron users (it's currently the <strike>12th</strike> 6th most requested app on their App Wishlist), see:\\r\\n\\r\\nhttps://forum.cloudron.io/post/29826 \\r\\n\\r\\nFor info about packaging apps for Cloudron see:\\r\\n\\r\\nhttps://forum.cloudron.io/post/10712",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "self-hosting",
                                  "description": ""
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83D\\uDC8E Bounty",
                                  "description": "A bounty on Algora.io"
                                },
                                {
                                  "name": "\\uD83D\\uDCB0 Rewarded",
                                  "description": "Rewarded bounties on Algora.io"
                                },
                                {
                                  "name": "\\uD83D\\uDE4B\\uD83C\\uDFFB‍♂️help wanted",
                                  "description": "Help from the community is appreciated"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1892847006,
                              "number": 11310,
                              "title": "[CAL-2456] Create testcases to cover \\"Connect selected calendar to credential\\" ",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11310",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1046695,
                                "login": "emrysal",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-09-12T16:15:36Z",
                              "closedAt": null,
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nFollow up https://github.com/calcom/cal.com/pull/11283 with adding tests.\\n\\n<sub>[CAL-2456](https://linear.app/calcom/issue/CAL-2456/create-testcases-to-cover-connect-selected-calendar-to-credential)</sub>",
                              "labels": [
                                {
                                  "name": "Medium priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "automated-tests",
                                  "description": "area: unit tests, e2e tests, playwright"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1898017205,
                              "number": 11380,
                              "title": "[CAL-2474] E2E: Test email embed",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11380",
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
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-09-15T09:04:58Z",
                              "closedAt": null,
                              "body": "Extend these Embed Code Generator tests  to add Email Embed tests. [https://github.com/calcom/cal.com/blob/25d0c294942020aa7428d339cc11dbd8c1f16247/apps/web/playwright/embed-code-generator.e2e.ts#L114](https://github.com/calcom/cal.com/blob/25d0c294942020aa7428d339cc11dbd8c1f16247/apps/web/playwright/embed-code-generator.e2e.ts#L114)\\nShould select a few dates and slots and verify that the corresponding html is available in the textarea for copying\\n\\nMotivation: Something that could have avoided [this](11379) issue\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2474](https://linear.app/calcom/issue/CAL-2474/e2e-test-email-embed)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "automated-tests",
                                  "description": "area: unit tests, e2e tests, playwright"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1917712505,
                              "number": 11586,
                              "title": "[CAL-2541] Fix the UX/UI of our filters on the booking page",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11586",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 4536123,
                                "login": "ciaranha",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4536123?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-09-28T14:48:06Z",
                              "closedAt": null,
                              "body": "**Current problem**\\n\\nThe current filters jump between left and right aligned on tablet vs desktop and don't align with design.\\n\\n**Solution**\\n\\nMatch the filter UI/UX to Figma.\\n\\n[View in Figma](https://www.figma.com/file/UajFu4M1APhn2AywAEJkJr/New-Features?type=design&node-id=7869%3A93929&mode=dev)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2541](https://linear.app/calcom/issue/CAL-2541/fix-the-uxui-of-our-filters-on-the-booking-page)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "ui",
                                  "description": "area: UI, frontend, button, form, input"
                                },
                                {
                                  "name": "⚡ Quick Wins",
                                  "description": "A collection of quick wins/quick fixes that are less than 30 minutes of work"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "\\uD83C\\uDFA8 needs design",
                                  "description": "Before engineering kick-off, a designer needs to submit a mockup"
                                },
                                {
                                  "name": "\\uD83E\\uDDF9 Improvements",
                                  "description": "Improvements to existing features. Mostly UX/UI"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ]
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
                                "isRegistered": false,
                                "id": null
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
                              "assignees": []
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
                              "issueCount": 5
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
                .jsonPath("$.totalItemNumber").isEqualTo(0);
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
                        "languageIds", "ca600cac-0f45-44e9-a6e8-25e21b0c6887,75ce6b37-8610-4600-8d2d-753b50aeda1e",
                        "direction", "DESC"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 7,
                          "totalItemNumber": 33,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 2023474944,
                              "number": 12674,
                              "title": "Impossible to set up multiple availability slots on the same day",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12674",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 20115649,
                                "login": "FlxMgdnz",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/20115649?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-12-04T10:06:58Z",
                              "closedAt": null,
                              "body": "### Issue Summary\\r\\n\\r\\nWhen setting up my availability, I cannot specify e.g. Monday 9–11 am and 3–5 pm. When adding a second time slot, the dropdown for the From time is limited to 11:45 for the example given above.\\r\\n\\r\\n![image](https://github.com/calcom/cal.com/assets/20115649/c2da2e50-e964-4301-92d7-858cd567d1e8)\\r\\n\\r\\n### Technical details\\r\\n\\r\\nChrome 119 on Windows 11",
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
                                  "name": "❗️invalid",
                                  "description": "This doesn't seem right"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 2022224998,
                              "number": 12640,
                              "title": "Edit Location modal truncates fields/cannot scroll on mobile",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12640",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 902488,
                                "login": "twolfson",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/902488?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-12-02T21:53:11Z",
                              "closedAt": null,
                              "body": "### Issue Summary\\r\\n\\r\\nWhen I receive a new booking and try to edit the location on my phone, I can choose a Meeting URL but then the successive field for Meeting URL is truncated.\\r\\n\\r\\nIt's visible once I switch to landscape mode, but that also seems quite broken since the previous fields aren't visible.\\r\\n\\r\\nScreenshot at bottom\\r\\n\\r\\nI did see #5987 and #3476 but those seem resolved already, and am unable to find others that are the same issue\\r\\n\\r\\n### Steps to Reproduce\\r\\n\\r\\n1. On a mobile device with a small screen\\r\\n    - I'm using a Sony Xperia XZ2 Compact, resolution 1080x2160\\r\\n    - There's a similar issue in 2160x1080 but it's different in that the fields pop out at the bottom\\r\\n3. Navigate to an existing listing in https://app.cal.com/bookings/upcoming\\r\\n4. Click on \\"Edit -> Edit Location\\"\\r\\n5. Select \\"Link meeting\\"\\r\\n6. See that new \\"Provide a Meeting Link\\" field is not visible and cannot be scrolled to\\r\\n\\r\\n### Actual Results\\r\\n\\r\\n- Field is getting cut off and not accessible via scrolling\\r\\n\\r\\n### Expected Results\\r\\n\\r\\n- All fields should be visible or scrollable\\r\\n\\r\\n### Technical details\\r\\n\\r\\n- Browser version: Chrome on Android, 119.0.6045.163\\r\\n- Node.js version: N/A, using https://app.cal.com/\\r\\n\\r\\n### Evidence\\r\\n\\r\\nScreenshots:\\r\\n\\r\\n![Screenshot_20231202-134150](https://github.com/calcom/cal.com/assets/902488/abedcf57-72b5-4a9e-817e-e096dcbacb51)\\r\\n\\r\\n![Screenshot_20231202-134211](https://github.com/calcom/cal.com/assets/902488/a352f7c1-ffc0-41c2-aa8c-75eb4658656a)\\r\\n\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 2021483969,
                              "number": 12627,
                              "title": "[CAL-2782] use \\"Email validation\\" from sendgrid for Booking page",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12627",
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
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-12-01T19:47:00Z",
                              "closedAt": null,
                              "body": "add a new section in advanced settings:\\n\\n![CleanShot 2023-12-01 at 19 48 44@2x](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/8496c432-f47b-4efd-8687-410e3c8315cb/e15797d6-2d79-41aa-a59e-fc054e6a756b?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy84NDk2YzQzMi1mNDdiLTRlZmQtODY4Ny00MTBlM2M4MzE1Y2IvZTE1Nzk3ZDYtMmQ3OS00MWFhLWE1OWUtZmMwNTRlNmE3NTZiIiwiaWF0IjoxNzAxNDYwMTQxLCJleHAiOjE3MDE1NDY1NDF9.8WahifRcaka7eCZrQIONnV9JYOmC2-zyzO9X3YGby74)\\n\\nif checked, use Sendgrids Email validation and if the API returns with a potential spam email, the booking should \\"require confirmation\\"\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2782](https://linear.app/calcom/issue/CAL-2782/use-email-validation-from-sendgrid-for-booking-page)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 2019022252,
                              "number": 12608,
                              "title": "[CAL-2780] Refactor database layer",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12608",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 2538462,
                                "login": "keithwillcode",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/2538462?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-30T16:21:53Z",
                              "closedAt": null,
                              "body": "Our current codebase tightly couples Prisma database calls throughout the application, making it challenging to write effective unit tests and increasing the risk of unintended side effects during database interaction. To address this, we propose a refactoring initiative to encapsulate Prisma database calls behind facades. These facades will provide a clean and testable interface, improving code maintainability, scalability, and the overall quality of our application.\\n\\n### Desired Features:\\n\\n* **Facade Implementation**: Develop facades that encapsulate Prisma database calls within distinct modules, offering a clean and organized API for database interactions. e.g. Repository Pattern\\n* **Testability**: Design facades to be easily testable, allowing developers to write comprehensive unit tests for business logic without directly interacting with the database layer.\\n* **Code Organization**: Improve code organization by separating database-related concerns from business logic, promoting a more modular and maintainable architecture. Abstraction of Prisma Queries: Abstract away Prisma-specific query details within facades, ensuring that changes to the database schema or ORM do not ripple through the entire codebase.\\n* **Documentation**: Provide clear documentation for developers on how to use the new facades, emphasizing their role in enhancing testability and code organization.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2780](https://linear.app/calcom/issue/CAL-2780/refactor-database-layer)</sub>",
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
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "\\uD83D\\uDCBB refactor",
                                  "description": null
                                },
                                {
                                  "name": "\\uD83D\\uDEA7 wip / in the making",
                                  "description": null
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 2017770882,
                              "number": 12602,
                              "title": "[CAL-2781] Cancelling Access to google account gives Json output",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12602",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 98404323,
                                "login": "JIbil12",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98404323?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-30T02:46:37Z",
                              "closedAt": null,
                              "body": "Found a bug? Please fill out the sections below. \\uD83D\\uDC4D\\r\\n\\r\\n### Issue Summary\\r\\nFor the first time i Login with google, website brings to the step 1 of 5 , while completing this on step 2 connecting to google calendar if i  cancel the access \\" Cal.com wants access to your Google Account \\"  , It brings to an error \\" https://app.cal.com/api/integrations/googlecalendar/callback?error=access_denied&state=%7B%22returnTo%22:%22https://app.cal.com/apps/installed%22%7D \\" Showing {\\"message\\":\\"`code` must be a string\\"}\\r\\n\\r\\nA summary of the issue. This needs to be a clear detailed-rich summary.\\r\\n\\r\\n### Steps to Reproduce\\r\\n\\r\\n1. Login\\r\\n2. Connect to Calendar Pop ups Step 1/5\\r\\n3. Choose google calendar and connect\\r\\n4. Choose account\\r\\n5. If cancel the access error shows up\\r\\n\\r\\nhttps://github.com/calcom/cal.com/assets/98404323/22c35115-be86-4625-8b96-a9cdd0a15b61\\r\\n\\r\\n\\r\\n\\r\\nAny other relevant information. For example, why do you consider this a bug and what did you expect to happen instead?\\r\\n\\r\\n### Actual Results\\r\\n\\r\\n- What's happening right now that is different from what is expected\\r\\n\\r\\n### Expected Results\\r\\n\\r\\n- This is an ideal result that the system should get after the tests are performed\\r\\n\\r\\n### Technical details\\r\\n\\r\\n- Browser version, screen recording, console logs, network requests: You can make a recording with [Bird Eats Bug](https://birdeatsbug.com/).\\r\\n- Node.js version\\r\\n- Anything else that you think could be an issue.\\r\\n\\r\\n### Evidence\\r\\n\\r\\n- How was this tested? This is quite mandatory in terms of bugs. Providing evidence of your testing with screenshots or/and videos is an amazing way to prove the bug and a troubleshooting chance to find the solution.\\r\\n\\n\\n<sub>[CAL-2781](https://linear.app/calcom/issue/CAL-2781/cancelling-access-to-google-account-gives-json-output)</sub>",
                              "labels": [
                                {
                                  "name": "High priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "calendar-apps",
                                  "description": "area: calendar, google calendar, outlook, lark, microsoft 365, apple calendar"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ]
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(HACKATHON_BY_ID_PROJECT_ISSUES.formatted("e06aeec6-cec6-40e1-86cb-e741e0dacf25"), Map.of(
                        "statuses", "OPEN",
                        "languageIds", "ca600cac-0f45-44e9-a6e8-25e21b0c6887,75ce6b37-8610-4600-8d2d-753b50aeda1e"
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
                              "issueCount": 33
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
                          "totalItemNumber": 10,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 2002697050,
                              "number": 12446,
                              "title": "[CAL-2747] Missing dependency node-mocks-http",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12446",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 17303782,
                                "login": "krumware",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/17303782?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-20T17:40:32Z",
                              "closedAt": "2023-12-03T18:52:51Z",
                              "body": "Found a bug? Please fill out the sections below. \\uD83D\\uDC4D\\r\\n\\r\\n### Issue Summary\\r\\n\\r\\nWhen building from scratch, it appears that there is a missing dependency in the yarn/npm lockfile.\\r\\n\\r\\n```\\r\\n#16 206.7 @calcom/web:build: ./test/utils/bookingScenario/createMockNextJsRequest.ts:1:29\\r\\n#16 206.7 @calcom/web:build: Type error: Cannot find module 'node-mocks-http' or its corresponding type declarations.\\r\\n#16 206.7 @calcom/web:build: \\r\\n#16 206.7 @calcom/web:build: > 1 | import { createMocks } from \\"node-mocks-http\\";\\r\\n#16 206.7 @calcom/web:build:     |                             ^\\r\\n#16 206.7 @calcom/web:build:   2 | \\r\\n#16 206.7 @calcom/web:build:   3 | import type {\\r\\n#16 206.7 @calcom/web:build:   4 |   CustomNextApiRequest,\\r\\n```\\r\\n\\r\\n### Steps to Reproduce\\r\\n\\r\\n1. Build the docker image\\r\\n\\r\\n### Actual Results\\r\\n\\r\\nImage will not build\\r\\n\\r\\n### Expected Results\\r\\n\\r\\nImage should build\\r\\n\\r\\n### Technical details\\r\\n\\r\\nn/a\\r\\n\\r\\n### Evidence\\r\\n\\r\\nBuild log:\\r\\nhttps://github.com/calcom/docker/actions/runs/6886177759/job/18738607721\\r\\n\\n\\n<sub>[CAL-2747](https://linear.app/calcom/issue/CAL-2747/missing-dependency-node-mocks-http)</sub>",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "ci",
                                  "description": "area: CI, DX, pipeline, github actions"
                                },
                                {
                                  "name": "⬆️ dependencies",
                                  "description": "Pull requests that update a dependency file"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1967171318,
                              "number": 12136,
                              "title": "Matomo analytics ",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12136",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 2198793,
                                "login": "joshua-russell",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/2198793?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-10-29T21:59:28Z",
                              "closedAt": "2023-12-03T15:49:02Z",
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\n<!--\\r\\n  Provide a clear and concise description of what the problem is.\\r\\n  For example, \\"I'm always frustrated when...\\"\\r\\n-->\\r\\n\\r\\nPlease add support for [Matomo Analytics](https://matomo.org/). It's another privacy friendly analytics platform. \\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\n<!--\\r\\n  Provide a clear and concise description of what you want to happen.\\r\\n-->\\r\\n\\r\\nAdd Matomo to the list of available analytics apps. \\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\n<!--\\r\\n  Let us know about other solutions you've tried or researched.\\r\\n-->\\r\\n\\r\\nI have looked at the other analytics options available but I already self-host matomo. \\r\\n\\r\\n### Additional context\\r\\n\\r\\n<!--\\r\\n  Is there anything else you can add about the proposal?\\r\\n  You might want to link to related issues here, if you haven't already.\\r\\n-->\\r\\n\\r\\nIf there is already a way to do this, please share? \\r\\n\\r\\n### Requirement/Document\\r\\n\\r\\n<!--\\r\\n  Is there any type of document that could support that feature?\\r\\n-->\\r\\n\\r\\n[How to embed the matomo tracking code](https://matomo.org/faq/new-to-piwik/how-do-i-embed-the-matomo-tracking-code-in-my-website-cms/)\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "app-store",
                                  "description": "area: app store, apps, calendar integrations, google calendar, outlook, lark, apple calendar"
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1981399962,
                              "number": 12264,
                              "title": "Only show next available slot",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12264",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 4146384,
                                "login": "notflip",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4146384?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-07T13:45:29Z",
                              "closedAt": "2023-12-03T00:07:07Z",
                              "body": "### Is your proposal related to a problem?\\r\\nIs it possible, or is there any feature request for a case in which you show the next available timeslot, when it's filled, the next one opens up. This is a much requested feature by medical people who want to bundle all their appointments close together.\\r\\n\\r\\n### Describe the solution you'd like\\r\\nA checkbox to enable the feature, and when enabled we start showing 1 slot closest to the starting time of the range, and only show the next slot when the previous one is filled.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "❓ needs spec",
                                  "description": "Needs more specifications"
                                },
                                {
                                  "name": "\\uD83C\\uDFA8 needs design",
                                  "description": "Before engineering kick-off, a designer needs to submit a mockup"
                                },
                                {
                                  "name": "\\uD83D\\uDEA8 needs approval",
                                  "description": "This feature request has not been reviewed yet by the Product Team and needs approval beforehand"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 4536123,
                                  "login": "ciaranha",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4536123?v=4"
                                }
                              ]
                            },
                            {
                              "id": 1963778975,
                              "number": 12107,
                              "title": "Display long durations in hours on the booking page",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12107",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 1055691,
                                "login": "nicwortel",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1055691?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-10-26T15:03:23Z",
                              "closedAt": "2023-12-02T18:27:12Z",
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWhen using Cal.com to schedule longer events (such as workshops, trainings, etc. which take a half or full working day), displaying durations on the booking page in minutes (such as 240 minutes or 480 minutes) is confusing.\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nOn the booking page, durations longer than a certain threshold should be rendered as `X hours` or `X hours Y mins` instead of in minutes.\\r\\n\\r\\nMy suggestion would be to do this for durations of 60 minutes and more, meaning that 60 minutes would be displayed as `1 hour` and 90 minutes would be displayed as `1 hour 30 mins`, but that is open for discussion.\\r\\n\\r\\nThis applies to event types with a fixed duration as well as event types where the booker can choose from multiple duration options.\\r\\n\\r\\nCurrent | Proposed solution\\r\\n---|---\\r\\n![afbeelding](https://github.com/calcom/cal.com/assets/1055691/76294541-676e-4d3c-adcd-8177cd07f471) | ![afbeelding](https://github.com/calcom/cal.com/assets/1055691/01b69601-953a-4016-8837-0b29c7272148)\\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\nThe scope of this issue is intentionally limited to the booking page. I initially also considered the event type management page, but that would introduce a lot of complexity in order to handle different input modes, standard durations vs events where the booker can choose from different durations, etc.\\r\\nA solution for the event type management page could be proposed in a separate issue, but for me personally that would have less priority than this one. As the owner of the event I just have to convert hours to minutes once when I setup the event, as compared to every booker having to convert the number of minutes to hours.\\r\\n\\r\\nInstead of the proposed threshold of 60 minutes a higher threshold could be considered. For example by setting it to 120 minutes, 120 minutes would be displayed as `2 hours` but 90 minutes would still be `90 mins`, which would avoid the complexity of singular/plural translations (needing different translations for `1 hour` and `2 hours`).\\r\\n\\r\\nAlternatively, the singular/plural issue could simply be ignored, always showing plural (such as `1 hours` or `1 hours 30 mins`), but that doesn't feel like a good solution. For minutes I think it is less of an issue, because I don't think having a duration of X hours and 1 minute will be a very common use case.\\r\\n\\r\\nI have also considered limiting this to very specific durations (for example a hard-coded mapping from 240 and 480 minutes to `4 hours` and `8 hours`), but I think a more generic solution would be preferable and will support more use cases.\\r\\n\\r\\n### Additional context\\r\\n\\r\\n#10663 introduced 240 and 480 minutes as options for multiple duration event types, resolving #10181.\\r\\nIn https://github.com/calcom/cal.com/issues/10181#issuecomment-1671588333 I suggested displaying longer durations in hours instead of minutes.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                },
                                {
                                  "name": "\\uD83E\\uDDF9 Improvements",
                                  "description": "Improvements to existing features. Mostly UX/UI"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 2000525280,
                              "number": 12427,
                              "title": "Add a functionality to remove filters in the booking section",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12427",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 62770475,
                                "login": "DikshaMakkar",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/62770475?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-18T18:17:12Z",
                              "closedAt": "2023-12-02T18:10:47Z",
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\n<!--\\r\\n  Provide a clear and concise description of what the problem is.\\r\\n  For example, \\"I'm always frustrated when...\\"\\r\\n-->\\r\\n\\r\\nThis is not related to any existing problem, its just a suggestion!\\r\\n\\r\\nI noticed that we are able to add filters like people and event type for booking but there is no option to remove these filters.\\r\\nAdding this feature to remove filter might improve user experience on cal.com.\\r\\n\\r\\n![image](https://github.com/calcom/cal.com/assets/62770475/e26f19e7-9d85-4764-9c73-90b4c4986740)\\r\\n\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\n<!--\\r\\n  Provide a clear and concise description of what you want to happen.\\r\\n-->\\r\\n\\r\\nWe can have a option like: **clear all filters** or a **X** beside individual filters to remove that specific filter.\\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\n<!--\\r\\n  Let us know about other solutions you've tried or researched.\\r\\n-->\\r\\n\\r\\nThese filters can be removed by refreshing the page but I believe its not a good idea to refresh page each time you want to remove filter and there should be a handy option to add and remove filters.\\r\\n\\r\\nThank you!\\r\\n\\r\\n---\\r\\n##### House rules\\r\\n- If this issue has a `\\uD83D\\uDEA8 needs approval` label, don't start coding yet. Wait until a core member approves feature request by removing this label, then you can start coding.\\r\\n  - For clarity: Non-core member issues automatically get the `\\uD83D\\uDEA8 needs approval` label.\\r\\n  - Your feature ideas are invaluable to us! However, they undergo review to ensure alignment with the product's direction.\\r\\n",
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "osshack",
                                  "description": "Submission for 2023 OSShack"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
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
                              "issueCount": 10
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
                        "search", "CAL-2781",
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
                              "id": 2017770882,
                              "number": 12602,
                              "title": "[CAL-2781] Cancelling Access to google account gives Json output",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12602",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "author": {
                                "githubUserId": 98404323,
                                "login": "JIbil12",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98404323?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2023-11-30T02:46:37Z",
                              "closedAt": null,
                              "body": "Found a bug? Please fill out the sections below. \\uD83D\\uDC4D\\r\\n\\r\\n### Issue Summary\\r\\nFor the first time i Login with google, website brings to the step 1 of 5 , while completing this on step 2 connecting to google calendar if i  cancel the access \\" Cal.com wants access to your Google Account \\"  , It brings to an error \\" https://app.cal.com/api/integrations/googlecalendar/callback?error=access_denied&state=%7B%22returnTo%22:%22https://app.cal.com/apps/installed%22%7D \\" Showing {\\"message\\":\\"`code` must be a string\\"}\\r\\n\\r\\nA summary of the issue. This needs to be a clear detailed-rich summary.\\r\\n\\r\\n### Steps to Reproduce\\r\\n\\r\\n1. Login\\r\\n2. Connect to Calendar Pop ups Step 1/5\\r\\n3. Choose google calendar and connect\\r\\n4. Choose account\\r\\n5. If cancel the access error shows up\\r\\n\\r\\nhttps://github.com/calcom/cal.com/assets/98404323/22c35115-be86-4625-8b96-a9cdd0a15b61\\r\\n\\r\\n\\r\\n\\r\\nAny other relevant information. For example, why do you consider this a bug and what did you expect to happen instead?\\r\\n\\r\\n### Actual Results\\r\\n\\r\\n- What's happening right now that is different from what is expected\\r\\n\\r\\n### Expected Results\\r\\n\\r\\n- This is an ideal result that the system should get after the tests are performed\\r\\n\\r\\n### Technical details\\r\\n\\r\\n- Browser version, screen recording, console logs, network requests: You can make a recording with [Bird Eats Bug](https://birdeatsbug.com/).\\r\\n- Node.js version\\r\\n- Anything else that you think could be an issue.\\r\\n\\r\\n### Evidence\\r\\n\\r\\n- How was this tested? This is quite mandatory in terms of bugs. Providing evidence of your testing with screenshots or/and videos is an amazing way to prove the bug and a troubleshooting chance to find the solution.\\r\\n\\n\\n<sub>[CAL-2781](https://linear.app/calcom/issue/CAL-2781/cancelling-access-to-google-account-gives-json-output)</sub>",
                              "labels": [
                                {
                                  "name": "High priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "calendar-apps",
                                  "description": "area: calendar, google calendar, outlook, lark, microsoft 365, apple calendar"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 53316345,
                                  "login": "Udit-takkar",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/53316345?v=4"
                                }
                              ]
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
                          "projects": []
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
                .jsonPath("$.totalPageNumber").isEqualTo(0);

        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted("00490be6-2c03-4720-993b-aea3e07edd81"), Map.of(
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
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1270688337,
                              "number": 196,
                              "title": "Exercise on the ec_op builtin",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/196",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-06-14T11:53:26Z",
                              "closedAt": null,
                              "body": "## Description\\r\\n  Add an exercise in the builtin tracks about elliptic curve operations using the `EcOpBuiltin` struct.\\r\\n \\r\\nResource:\\r\\n - [ec_op cairo source](https://github.com/starkware-libs/cairo-lang/blob/167b28bcd940fd25ea3816204fa882a0b0a49603/src/starkware/cairo/common/ec.cairo#L99)\\r\\n\\r\\n## Acceptance criteria\\r\\n - Add an exercise in the builtins track folder on the `ec_op` builtin\\r\\n - The exercise compiles and tests pass\\r\\n - An exercise patch is generated in `.patch/`\\r\\n - The exercise list in `src/exercises/__init__.py` is updated\\r\\n",
                              "labels": [
                                {
                                  "name": "Context: isolated",
                                  "description": "no previous knowledge of the codebase required"
                                },
                                {
                                  "name": "Difficulty: hard",
                                  "description": "require extensive knowledge about the field"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1270661065,
                              "number": 194,
                              "title": "Exercise on the output builtin",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/194",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-06-14T11:29:40Z",
                              "closedAt": null,
                              "body": "## Description\\r\\n  Add an exercise in the builtin tracks about output processing using the `output_ptr` variable and serializing functions.\\r\\n  Find a way to design a Cairo test to verify the output is effectively assigned.\\r\\n \\r\\nResource:\\r\\n - [builtins doc](https://www.cairo-lang.org/docs/how_cairo_works/builtins.html)\\r\\n - [output doc](https://www.cairo-lang.org/docs/how_cairo_works/program_input_and_output.html#id2)\\r\\n - [serialize.cairo](https://github.com/starkware-libs/cairo-lang/blob/master/src/starkware/cairo/common/serialize.cairo)\\r\\n\\r\\n## Acceptance criteria\\r\\n - Add an exercise in the builtins track folder on the output builtin\\r\\n - The exercise compiles and tests pass\\r\\n - An exercise patch is generated in `.patch/`\\r\\n - The exercise list in `src/exercises/__init__.py` is updated\\r\\n",
                              "labels": [
                                {
                                  "name": "Context: isolated",
                                  "description": "no previous knowledge of the codebase required"
                                },
                                {
                                  "name": "Difficulty: intermediate",
                                  "description": "mobilise some notions about the field, but can be learned while doing"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1262933369,
                              "number": 181,
                              "title": "VSCode extension to automatically move on to the next exercise",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/181",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 4435377,
                                "login": "Bernardstanislas",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                                "isRegistered": true,
                                "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                              },
                              "createdAt": "2022-06-07T08:34:10Z",
                              "closedAt": null,
                              "body": null,
                              "labels": [
                                {
                                  "name": "Context: coupled",
                                  "description": "interact with some parts of the codebase"
                                },
                                {
                                  "name": "Difficulty: hard",
                                  "description": "require extensive knowledge about the field"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: js",
                                  "description": "javascript/typescript"
                                },
                                {
                                  "name": "Techno: python",
                                  "description": "python"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1243347024,
                              "number": 139,
                              "title": "Exercise on the common math operations",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/139",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-05-20T15:56:33Z",
                              "closedAt": null,
                              "body": "## Description\\r\\nThe common library contains predefined functions to handle common math operations. \\r\\nSuch as `<`, `<=`, `> 0`, range checks, absolute value, felt division.\\r\\nProvide an exercise to learn how to use those functions.\\r\\n\\r\\nResource:\\r\\n - https://perama-v.github.io/cairo/examples/math/\\r\\n\\r\\n## Acceptance criteria\\r\\n - The exercise showcase math common library functions\\r\\n - The contract compiles and tests pass\\r\\n - An exercise patch is generated",
                              "labels": [
                                {
                                  "name": "Difficulty: easy",
                                  "description": "anybody can understand it"
                                },
                                {
                                  "name": "Duration: under a day",
                                  "description": "wil take up to one day"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues_as_registered_user() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted("00490be6-2c03-4720-993b-aea3e07edd81"), Map.of(
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
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "issues": [
                            {
                              "id": 1270688337,
                              "number": 196,
                              "title": "Exercise on the ec_op builtin",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/196",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-06-14T11:53:26Z",
                              "closedAt": null,
                              "body": "## Description\\r\\n  Add an exercise in the builtin tracks about elliptic curve operations using the `EcOpBuiltin` struct.\\r\\n \\r\\nResource:\\r\\n - [ec_op cairo source](https://github.com/starkware-libs/cairo-lang/blob/167b28bcd940fd25ea3816204fa882a0b0a49603/src/starkware/cairo/common/ec.cairo#L99)\\r\\n\\r\\n## Acceptance criteria\\r\\n - Add an exercise in the builtins track folder on the `ec_op` builtin\\r\\n - The exercise compiles and tests pass\\r\\n - An exercise patch is generated in `.patch/`\\r\\n - The exercise list in `src/exercises/__init__.py` is updated\\r\\n",
                              "labels": [
                                {
                                  "name": "Context: isolated",
                                  "description": "no previous knowledge of the codebase required"
                                },
                                {
                                  "name": "Difficulty: hard",
                                  "description": "require extensive knowledge about the field"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1270661065,
                              "number": 194,
                              "title": "Exercise on the output builtin",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/194",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-06-14T11:29:40Z",
                              "closedAt": null,
                              "body": "## Description\\r\\n  Add an exercise in the builtin tracks about output processing using the `output_ptr` variable and serializing functions.\\r\\n  Find a way to design a Cairo test to verify the output is effectively assigned.\\r\\n \\r\\nResource:\\r\\n - [builtins doc](https://www.cairo-lang.org/docs/how_cairo_works/builtins.html)\\r\\n - [output doc](https://www.cairo-lang.org/docs/how_cairo_works/program_input_and_output.html#id2)\\r\\n - [serialize.cairo](https://github.com/starkware-libs/cairo-lang/blob/master/src/starkware/cairo/common/serialize.cairo)\\r\\n\\r\\n## Acceptance criteria\\r\\n - Add an exercise in the builtins track folder on the output builtin\\r\\n - The exercise compiles and tests pass\\r\\n - An exercise patch is generated in `.patch/`\\r\\n - The exercise list in `src/exercises/__init__.py` is updated\\r\\n",
                              "labels": [
                                {
                                  "name": "Context: isolated",
                                  "description": "no previous knowledge of the codebase required"
                                },
                                {
                                  "name": "Difficulty: intermediate",
                                  "description": "mobilise some notions about the field, but can be learned while doing"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1262933369,
                              "number": 181,
                              "title": "VSCode extension to automatically move on to the next exercise",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/181",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 4435377,
                                "login": "Bernardstanislas",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                                "isRegistered": true,
                                "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                              },
                              "createdAt": "2022-06-07T08:34:10Z",
                              "closedAt": null,
                              "body": null,
                              "labels": [
                                {
                                  "name": "Context: coupled",
                                  "description": "interact with some parts of the codebase"
                                },
                                {
                                  "name": "Difficulty: hard",
                                  "description": "require extensive knowledge about the field"
                                },
                                {
                                  "name": "Duration: few days",
                                  "description": "will take a few days"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: js",
                                  "description": "javascript/typescript"
                                },
                                {
                                  "name": "Techno: python",
                                  "description": "python"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1243347024,
                              "number": 139,
                              "title": "Exercise on the common math operations",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/139",
                              "repo": {
                                "id": 480776993,
                                "owner": "onlydustxyz",
                                "name": "starklings",
                                "description": "An interactive tutorial to get you up and running with Starknet",
                                "htmlUrl": "https://github.com/onlydustxyz/starklings"
                              },
                              "author": {
                                "githubUserId": 98529704,
                                "login": "tekkac",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "createdAt": "2022-05-20T15:56:33Z",
                              "closedAt": null,
                              "body": "## Description\\r\\nThe common library contains predefined functions to handle common math operations. \\r\\nSuch as `<`, `<=`, `> 0`, range checks, absolute value, felt division.\\r\\nProvide an exercise to learn how to use those functions.\\r\\n\\r\\nResource:\\r\\n - https://perama-v.github.io/cairo/examples/math/\\r\\n\\r\\n## Acceptance criteria\\r\\n - The exercise showcase math common library functions\\r\\n - The contract compiles and tests pass\\r\\n - An exercise patch is generated",
                              "labels": [
                                {
                                  "name": "Difficulty: easy",
                                  "description": "anybody can understand it"
                                },
                                {
                                  "name": "Duration: under a day",
                                  "description": "wil take up to one day"
                                },
                                {
                                  "name": "State: open",
                                  "description": "ready for contribution"
                                },
                                {
                                  "name": "Techno: cairo",
                                  "description": "cairo"
                                },
                                {
                                  "name": "Type: feature",
                                  "description": "a new feature to implement"
                                },
                                {
                                  "name": "good first issue",
                                  "description": "Good for newcomers"
                                }
                              ],
                              "applicants": [],
                              "assignees": []
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

            final var applications = List.of(
                    // 1906904541L has 2 applicants on project 2
                    // 1652216317L has 2 applicants on project 1 and 1 applicant on project 2
                    fakeApplication(projectAppliedTo1, pierre, 1906921238L, 112L),
                    fakeApplication(projectAppliedTo2, pierre, 1906904541L, 113L),

                    fakeApplication(projectAppliedTo2, antho, 1906904541L, 112L),
                    fakeApplication(projectAppliedTo2, antho, 1906921238L, 113L),

                    fakeApplication(projectAppliedTo1, olivier, 1906921238L, 112L)
            );
            databaseHelper.executeInTransaction(() -> {
                applicationRepository.saveAll(applications);
                applications.forEach(a -> biProjectorAdapter.onApplicationCreated(new Application(
                        Application.Id.of(a.id()),
                        ProjectId.of(a.projectId()),
                        a.applicantId(),
                        a.origin(),
                        a.receivedAt(),
                        GithubIssue.Id.of(a.issueId()),
                        GithubComment.Id.of(a.commentId()),
                        a.commentBody()
                )));
            });
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
                              "totalPageNumber": 19,
                              "totalItemNumber": 56,
                              "hasMore": true,
                              "nextPageIndex": 1,
                              "issues": [
                                {
                                  "id": 1643865031,
                                  "number": 12,
                                  "title": "Documentation by AnthonyBuisset",
                                  "status": "OPEN",
                                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/12",
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
                                    "isRegistered": true,
                                    "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                                  },
                                  "createdAt": "2023-03-28T12:39:14Z",
                                  "closedAt": null,
                                  "body": "Real cool documentation",
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": []
                                },
                                {
                                  "id": 1643866301,
                                  "number": 13,
                                  "title": "Documentation by AnthonyBuisset",
                                  "status": "OPEN",
                                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/13",
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
                                    "isRegistered": true,
                                    "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                                  },
                                  "createdAt": "2023-03-28T12:40:02Z",
                                  "closedAt": null,
                                  "body": "Real cool documentation",
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": []
                                },
                                {
                                  "id": 1643867196,
                                  "number": 14,
                                  "title": "Documentation by AnthonyBuisset",
                                  "status": "COMPLETED",
                                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/14",
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
                                    "isRegistered": true,
                                    "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                                  },
                                  "createdAt": "2023-03-28T12:40:35Z",
                                  "closedAt": "2023-08-30T09:20:48Z",
                                  "body": "Real cool documentation",
                                  "labels": [],
                                  "applicants": [],
                                  "assignees": [
                                    {
                                      "githubUserId": 43467246,
                                      "login": "AnthonyBuisset",
                                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                    }
                                  ]
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
