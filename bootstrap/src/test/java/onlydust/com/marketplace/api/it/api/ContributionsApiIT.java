package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ContributionType;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.ProjectContributorLabelFacadePort;
import onlydust.com.marketplace.project.domain.port.input.PullRequestFacadePort;
import org.assertj.core.api.AbstractListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ContributionsApiIT extends AbstractMarketplaceApiIT {
    private static final AtomicBoolean setupDone = new AtomicBoolean();
    static ProjectContributorLabel ogLabel;
    static UserAuthHelper.AuthenticatedUser projectLead;
    static UserAuthHelper.AuthenticatedUser recipient;
    static ProjectId kaaper = ProjectId.of("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");
    static RewardId rewardId;
    static Application application;
    static Long newIssueId;

    @Autowired
    ProjectContributorLabelFacadePort projectContributorLabelFacadePort;
    @Autowired
    PullRequestFacadePort pullRequestFacadePort;
    @Autowired
    PostgresBiProjectorAdapter postgresBiProjectorAdapter;

    @BeforeEach
    void setup() {
        if (setupDone.compareAndExchange(false, true)) return;

        final var olivier = userAuthHelper.authenticateOlivier();
        recipient = userAuthHelper.authenticatePierre();
        projectLead = userAuthHelper.authenticateAntho();
        ogLabel = projectContributorLabelFacadePort.createLabel(projectLead.userId(), kaaper, "OG");
        projectContributorLabelFacadePort.updateLabelsOfContributors(projectLead.userId(), kaaper,
                Map.of(olivier.user().getGithubUserId(), List.of(ogLabel.id())));
        githubHelper.addClosingIssue(43506983L, 1966796364L);
        databaseHelper.executeQuery("UPDATE indexer_exp.github_issues_assignees SET assigned_by_user_id = :assignedBy " +
                                    "WHERE issue_id = 1300430041 and user_id = 595505 ", Map.of("assignedBy", projectLead.githubUserId().value()));
        githubHelper.setMergedBy(43506983L, olivier.githubUserId().value());
        postgresBiProjectorAdapter.onContributionsChanged(ContributionUUID.of(UUID.fromString("0f8d789f-fbbd-3171-ad03-9b2b6f8d9174")));
        postgresBiProjectorAdapter.onContributionsChanged(ContributionUUID.of(UUID.fromString("f4db1d9b-4e1d-300c-9277-8d05824c804e")));

        final var fooRepo = githubHelper.createRepo("foo");
        newIssueId = at("2024-01-01T00:00:00Z", () -> githubHelper.createIssue(fooRepo.getId(), ZonedDateTime.parse("2024-01-01T00:00:00Z"), null, "OPEN",
                projectLead));

        rewardId = rewardHelper.create(kaaper, projectLead, recipient.githubUserId(), 123, CurrencyHelper.USDC, List.of(
                RequestRewardCommand.Item.builder()
                        .id("1300430041")
                        .number(68L)
                        .repoId(498695724L)
                        .type(RequestRewardCommand.Item.Type.issue)
                        .build()));

        rewardHelper.create(kaaper, projectLead, recipient.githubUserId(), 20, CurrencyHelper.USDC, List.of(
                RequestRewardCommand.Item.builder()
                        .id("1300430041")
                        .number(68L)
                        .repoId(498695724L)
                        .type(RequestRewardCommand.Item.Type.issue)
                        .build()));

        rewardHelper.create(kaaper, projectLead, projectLead.githubUserId(), 1000, CurrencyHelper.USDC, List.of(
                RequestRewardCommand.Item.builder()
                        .id("1300430041")
                        .number(68L)
                        .repoId(498695724L)
                        .type(RequestRewardCommand.Item.Type.issue)
                        .build()));

        application = at("2024-10-23T09:30:40.738086Z", () -> applicationHelper.create(kaaper, GithubIssue.Id.of(1300430041L), olivier.githubUserId()));
    }

    @Test
    void should_get_total_rewarded_amount_as_maintainer_and_callerTotalRewardedUsdAmount_as_recipient() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("0f8d789f-fbbd-3171-ad03-9b2b6f8d9174")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "uuid": "0f8d789f-fbbd-3171-ad03-9b2b6f8d9174",
                          "type": "ISSUE",
                          "githubNumber": 68,
                          "totalRewardedUsdAmount": 1154.43,
                          "callerTotalRewardedUsdAmount": 1010.0
                        }
                        """);
    }

    @Test
    void should_get_callerTotalRewardedUsdAmount_as_recipient() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("0f8d789f-fbbd-3171-ad03-9b2b6f8d9174")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + recipient.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "uuid": "0f8d789f-fbbd-3171-ad03-9b2b6f8d9174",
                          "type": "ISSUE",
                          "githubNumber": 68,
                          "totalRewardedUsdAmount": null,
                          "callerTotalRewardedUsdAmount": 144.43
                        }
                        """);
    }

    @Test
    void should_get_pr_contribution() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("f4db1d9b-4e1d-300c-9277-8d05824c804e")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "uuid": "f4db1d9b-4e1d-300c-9277-8d05824c804e",
                          "type": "PULL_REQUEST",
                          "repo": {
                            "id": 40652912,
                            "owner": "IonicaBizau",
                            "name": "node-cobol",
                            "description": ":tv: COBOL bridge for NodeJS which allows you to run COBOL code from NodeJS.",
                            "htmlUrl": "https://github.com/IonicaBizau/node-cobol"
                          },
                          "githubAuthor": {
                            "githubUserId": 1814312,
                            "login": "krzkaczor",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/1814312?v=4"
                          },
                          "mergedBy": {
                            "githubUserId": 595505,
                            "login": "ofux",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                            "contacts": [
                              {
                                "channel": "TWITTER",
                                "contact": "https://twitter.com/fuxeto",
                                "visibility": "private"
                              },
                              {
                                "channel": "TELEGRAM",
                                "contact": "https://t.me/ofux",
                                "visibility": "private"
                              }
                            ]
                          },
                          "githubNumber": 8,
                          "githubStatus": "CLOSED",
                          "githubTitle": "Support for promises",
                          "githubHtmlUrl": "https://github.com/IonicaBizau/node-cobol/pull/8",
                          "githubBody": "https://github.com/IonicaBizau/node-cobol/issues/7\\n",
                          "githubLabels": null,
                          "lastUpdatedAt": "2023-11-24T10:32:31.763233Z",
                          "createdAt": "2015-08-27T11:38:25Z",
                          "completedAt": "2015-08-30T19:18:14Z",
                          "activityStatus": "DONE",
                          "project": {
                            "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                            "slug": "onlydust-marketplace",
                            "name": "OnlyDust Marketplace",
                            "logoUrl": null
                          },
                          "contributors": [
                            {
                              "githubUserId": 1814312,
                              "login": "krzkaczor",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1814312?v=4",
                              "since": "2024-10-17T14:03:10.967909Z"
                            }
                          ],
                          "applicants": null,
                          "languages": [
                            {
                              "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                              "slug": "javascript",
                              "name": "Javascript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                            }
                          ],
                          "linkedIssues": [
                            {
                              "contributionUuid": "4782d22d-be45-3253-a3b4-5688045632f7",
                              "type": "ISSUE",
                              "githubNumber": 49,
                              "githubStatus": "COMPLETED",
                              "githubTitle": "/* ... */ notation",
                              "githubHtmlUrl": "https://github.com/IonicaBizau/node-cobol/issues/49"
                            }
                          ],
                          "totalRewardedUsdAmount": null,
                          "githubCommentCount": 1
                        }
                        """);
    }

    @Test
    void should_get_pr_contribution_events() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID_EVENTS.formatted("f4db1d9b-4e1d-300c-9277-8d05824c804e")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "events": [
                            {
                              "timestamp": "2015-08-27T11:38:25Z",
                              "type": "PR_CREATED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": null
                            },
                            {
                              "timestamp": "2015-08-30T19:18:14Z",
                              "type": "PR_MERGED",
                              "assignee": null,
                              "mergedBy": {
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                "contacts": [
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/fuxeto",
                                    "visibility": "private"
                                  },
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/ofux",
                                    "visibility": "private"
                                  }
                                ]
                              },
                              "linkedIssueContributionUuid": null
                            },
                            {
                              "timestamp": "2023-10-29T01:01:47Z",
                              "type": "LINKED_ISSUE_CREATED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": "4782d22d-be45-3253-a3b4-5688045632f7"
                            },
                            {
                              "timestamp": "2023-10-29T07:41:49Z",
                              "type": "LINKED_ISSUE_CLOSED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": "4782d22d-be45-3253-a3b4-5688045632f7"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_issue_contribution() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("0f8d789f-fbbd-3171-ad03-9b2b6f8d9174")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.applicants[0].applicationId").isEqualTo(application.id().toString())
                .json("""
                        {
                          "uuid": "0f8d789f-fbbd-3171-ad03-9b2b6f8d9174",
                          "type": "ISSUE",
                          "repo": {
                            "id": 498695724,
                            "owner": "onlydustxyz",
                            "name": "marketplace-frontend",
                            "description": "Contributions marketplace backend services",
                            "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                          },
                          "githubAuthor": {
                            "githubUserId": 43467246,
                            "login": "AnthonyBuisset",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                          },
                          "githubNumber": 68,
                          "githubStatus": "COMPLETED",
                          "githubTitle": "when thread panick, need to restart the back-end",
                          "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/68",
                          "githubBody": null,
                          "githubLabels": [
                            {
                              "name": "Context: isolated",
                              "description": "no previous knowledge of the codebase required"
                            },
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
                              "name": "Techno: rust",
                              "description": "rust"
                            },
                            {
                              "name": "Type: bug",
                              "description": "fix a bug"
                            }
                          ],
                          "lastUpdatedAt": "2023-11-24T10:27:45.175549Z",
                          "githubId": "1300430041",
                          "createdAt": "2022-07-11T09:14:38Z",
                          "completedAt": "2022-08-05T08:07:52Z",
                          "activityStatus": "DONE",
                          "project": {
                            "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                            "slug": "kaaper",
                            "name": "kaaper",
                            "logoUrl": null
                          },
                          "contributors": [
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "since": "2024-10-17T14:03:10.967909Z",
                              "assignedBy": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "contacts": [
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/abuisset",
                                    "visibility": "public"
                                  },
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/abuisset",
                                    "visibility": "public"
                                  },
                                  {
                                    "channel": "DISCORD",
                                    "contact": "antho",
                                    "visibility": "public"
                                  }
                                ]
                              }
                            }
                          ],
                          "applicants": [
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "since": "2024-10-23T09:30:40.738Z"
                            }
                          ],
                          "languages": null,
                          "linkedIssues": null,
                          "totalRewardedUsdAmount": null
                        }
                        """);
    }

    @Test
    void should_get_issue_contribution_events() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID_EVENTS.formatted("0f8d789f-fbbd-3171-ad03-9b2b6f8d9174")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "events": [
                            {
                              "timestamp": "2024-10-17T14:03:10.967909Z",
                              "type": "ISSUE_ASSIGNED",
                              "assignee": {
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                "since": "2024-10-17T14:03:10.967909Z",
                                "assignedBy": {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "contacts": [
                                    {
                                      "channel": "TELEGRAM",
                                      "contact": "https://t.me/abuisset",
                                      "visibility": "public"
                                    },
                                    {
                                      "channel": "TWITTER",
                                      "contact": "https://twitter.com/abuisset",
                                      "visibility": "public"
                                    },
                                    {
                                      "channel": "DISCORD",
                                      "contact": "antho",
                                      "visibility": "public"
                                    }
                                  ]
                                }
                              },
                              "mergedBy": null,
                              "linkedIssueContributionUuid": null
                            },
                            {
                              "timestamp": "2022-08-05T08:07:52Z",
                              "type": "ISSUE_CLOSED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": null
                            },
                            {
                              "timestamp": "2022-07-11T09:14:38Z",
                              "type": "ISSUE_CREATED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": null
                            }
                          ]
                        }
                        """, true);
    }


    @Test
    void should_get_new_issue_contribution_events() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID_EVENTS.formatted(ContributionUUID.of(newIssueId).toString())))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "events": [
                            {
                              "timestamp": "2024-01-01T00:00:00Z",
                              "type": "ISSUE_CREATED",
                              "assignee": null,
                              "mergedBy": null,
                              "linkedIssueContributionUuid": null
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_get_linked_issue_by_id() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted("0446104b-7f0f-3852-8121-74d138bb9920")))
                // Then
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void should_show_archived_contribution_right_away() {
        // Given
        final var projectLead = userAuthHelper.authenticateAntho();
        final var contributionUuid = ContributionUUID.of(UUID.fromString("f4db1d9b-4e1d-300c-9277-8d05824c804e"));

        // When
        pullRequestFacadePort.updatePullRequest(projectLead.userId(), UpdatePullRequestCommand.builder()
                .id(contributionUuid)
                .archived(true)
                .build());

        // Then
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted(contributionUuid.value())))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.activityStatus").isEqualTo("ARCHIVED");

        // When
        pullRequestFacadePort.updatePullRequest(projectLead.userId(), UpdatePullRequestCommand.builder()
                .id(contributionUuid)
                .archived(false)
                .build());

        // Then
        client.get()
                .uri(getApiURI(CONTRIBUTIONS_BY_ID.formatted(contributionUuid.value())))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.activityStatus").isEqualTo("DONE");
    }

    @Test
    void should_get_contributions() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS, Map.of("pageSize", "1")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 4695,
                          "totalItemNumber": 4695,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributions": [
                            {
                              "uuid": "fb8a66f0-b4fc-353a-92ef-14d1a93b02b1",
                              "type": "ISSUE",
                              "repo": {
                                "id": 21339768,
                                "owner": "reactjs",
                                "name": "react-tutorial",
                                "description": "Code from the React tutorial.",
                                "htmlUrl": "https://github.com/reactjs/react-tutorial"
                              },
                              "githubAuthor": {
                                "githubUserId": 116432,
                                "login": "simonwhitaker",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/116432?v=4"
                              },
                              "githubNumber": 1,
                              "githubStatus": "COMPLETED",
                              "githubTitle": "Tutorial uses POST with SimpleHTTPServer",
                              "githubHtmlUrl": "https://github.com/reactjs/react-tutorial/issues/1",
                              "githubBody": "The tutorial suggests [using `python -m SimpleHTTPServer`](http://facebook.github.io/react/docs/tutorial.html#updating-state) to serve content, but subsequently introduces a mechanism for [submitting comments via a POST request](http://facebook.github.io/react/docs/tutorial.html#callbacks-as-props), which `SimpleHTTPServer` doesn't support. It appears that at this point the tutorial assumes you're using the sample node.js server packaged with [the tutorial's GitHub repo](https://github.com/reactjs/react-tutorial) but that isn't explained.\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-24T10:34:07.322779Z",
                              "createdAt": "2014-08-05T16:19:44Z",
                              "completedAt": "2015-01-16T21:21:47Z",
                              "activityStatus": "DONE",
                              "project": null,
                              "contributors": null,
                              "applicants": null,
                              "languages": null,
                              "linkedIssues": null,
                              "totalRewardedUsdAmount": null
                            }
                          ]
                        }
                        """);
    }

    @ParameterizedTest
    @CsvSource({"CREATED_AT,ASC", "CREATED_AT,DESC", "TYPE,ASC", "TYPE,DESC"})
    void should_sort_contributions(String sort, String direction) {
        var comparator = switch (sort) {
            case "CREATED_AT" -> comparing(ContributionActivityPageItemResponse::getCreatedAt);
            case "UPDATED_AT" -> comparing(ContributionActivityPageItemResponse::getLastUpdatedAt);
            case "TYPE" -> comparing(ContributionActivityPageItemResponse::getType);
            default -> throw new IllegalArgumentException("Invalid sort field: " + sort);
        };

        comparator = direction.equals("DESC") ? comparator.reversed() : comparator;

        assertContributions(Map.of("sort", sort, "sortDirection", direction))
                .isSortedAccordingTo(comparator);
    }

    @Test
    void should_filter_contributions() {
        assertContributions(Map.of("types", "PULL_REQUEST"))
                .extracting(ContributionActivityPageItemResponse::getType)
                .containsOnly(ContributionType.PULL_REQUEST);

        assertContributions(Map.of("ids", "00c1f2f1-1123-305b-95da-11da17765e39"))
                .extracting(ContributionActivityPageItemResponse::getUuid)
                .containsOnly(UUID.fromString("00c1f2f1-1123-305b-95da-11da17765e39"));

        assertContributions(Map.of("projectIds", "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e"))
                .extracting(c -> c.getProject().getName())
                .containsOnly("Cal.com");

        assertContributions(Map.of("projectSlugs", "calcom"))
                .extracting(c -> c.getProject().getName())
                .containsOnly("Cal.com");

        assertContributions(Map.of("statuses", "IN_PROGRESS"))
                .extracting(ContributionActivityPageItemResponse::getActivityStatus)
                .containsOnly(ContributionActivityStatus.IN_PROGRESS);

        assertContributions(Map.of("repoIds", "493591124"))
                .extracting(c -> c.getRepo().getName())
                .containsOnly("kaaper");

        assertContributions(Map.of("contributorIds", "43467246"))
                .extracting(ContributionActivityPageItemResponse::getContributors)
                .allMatch(contributors -> contributors.stream().anyMatch(c -> c.getLogin().equals("AnthonyBuisset")));

        assertContributions(Map.of("hasBeenRewarded", "true", "projectIds", kaaper.toString()))
                .extracting(ContributionActivityPageItemResponse::getTotalRewardedUsdAmount)
                .allMatch(a -> a.compareTo(BigDecimal.ZERO) > 0);

        assertContributions(Map.of("hasBeenRewarded", "false", "projectIds", kaaper.toString()))
                .extracting(ContributionActivityPageItemResponse::getTotalRewardedUsdAmount)
                .allMatch(a -> a.compareTo(BigDecimal.ZERO) == 0);

        assertContributions(Map.of("projectContributorLabelIds", ogLabel.id().value().toString()))
                .extracting(ContributionActivityPageItemResponse::getContributors)
                .allMatch(contributors -> contributors.stream().anyMatch(c -> c.getLogin().equals("ofux")));

        assertContributions(Map.of("rewardIds", rewardId.toString()))
                .extracting(ContributionActivityPageItemResponse::getTotalRewardedUsdAmount)
                .allMatch(a -> a.compareTo(BigDecimal.ZERO) > 0);

        assertContributions(Map.of("search", "KAAPER"))
                .extracting(r -> r.getProject().getName())
                .containsOnly("kaaper");

        assertContributions(Map.of("search", "coinbase", "showLinkedIssues", "false"))
                .extracting(ContributionActivityPageItemResponse::getUuid)
                .doesNotContain(UUID.fromString("0446104b-7f0f-3852-8121-74d138bb9920")) // Issue linked to PR below
                .contains(UUID.fromString("74ecdb48-9e1c-3f54-ab9c-df4dbd2a6ed3")); // PR linked to issue above

        assertContributions(Map.of("languageIds", "1109d0a2-1143-4915-a9c1-69e8be6c1bea"))
                .extracting(ContributionActivityPageItemResponse::getLanguages)
                .allMatch(languages -> languages.stream().anyMatch(l -> l.getName().equals("Javascript")));

        assertContributions(Map.of("fromDate", "2022-07-11", "toDate", "2022-07-12"))
                .extracting(ContributionActivityPageItemResponse::getCreatedAt)
                .allMatch(d -> d.isAfter(ZonedDateTime.parse("2022-07-11T00:00:00Z")) && d.isBefore(ZonedDateTime.parse("2022-07-13T00:00:00Z")));

        assertContributions(Map.of("dataSource", "ONLYDUST"))
                .extracting(ContributionActivityPageItemResponse::getProject)
                .allMatch(Objects::nonNull);

        assertContributions(Map.of("dataSource", "ALL"))
                .extracting(ContributionActivityPageItemResponse::getProject)
                .contains((ProjectLinkResponse) null);

        assertContributions(Map.of("applicantIds", "595505"))
                .extracting(ContributionActivityPageItemResponse::getApplicants)
                .allMatch(applicants -> applicants.stream().anyMatch(c -> c.getLogin().equals("ofux")));

    }

    private AbstractListAssert<?, ? extends List<? extends ContributionActivityPageItemResponse>, ContributionActivityPageItemResponse, ?> assertContributions(Map<String, String> params) {
        final var q = new HashMap<String, String>();
        q.put("pageSize", "80");
        q.putAll(params);

        final var contributions = client.get()
                .uri(getApiURI(CONTRIBUTIONS, q))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ContributionActivityPageResponse.class)
                .returnResult()
                .getResponseBody()
                .getContributions();

        return assertThat(contributions).isNotEmpty();
    }
}
