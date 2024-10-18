package onlydust.com.marketplace.api.it.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TagProject
public class ProjectGetInsightsApiIT extends AbstractMarketplaceApiIT {
    private final static String KAAPER = "298a547f-ecb6-4ab2-8975-68f4e9bf7b39";

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void should_get_staled_contributions() {
        // Given
        final String jwt = userAuthHelper.authenticateAntho().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_INSIGHTS_STALED_CONTRIBUTIONS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + jwt)
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
                          "contributions": [
                            {
                              "uuid": null,
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4"
                              },
                              "githubNumber": 1514,
                              "githubStatus": "DRAFT",
                              "githubTitle": "Migrate Profile mutation",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1514",
                              "githubBody": null,
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-12-01T16:53:55Z",
                              "id": "662217b394e92161dac7470d717508a9287bb253e9fc8d9206f750ee4b1a9f35",
                              "createdAt": "2023-12-01T16:53:55Z",
                              "completedAt": null,
                              "status": "IN_PROGRESS",
                              "githubPullRequestReviewState": "PENDING_REVIEWER",
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "links": []
                            },
                            {
                              "uuid": null,
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4"
                              },
                              "githubNumber": 1503,
                              "githubStatus": "DRAFT",
                              "githubTitle": "B 1178 build new contributions tab",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1503",
                              "githubBody": null,
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-30T16:32:31Z",
                              "id": "440ada773bbb1c112e039324edac9bd2e2dd612d2c7b99d9cc73aaa4ad483c11",
                              "createdAt": "2023-11-30T16:32:31Z",
                              "completedAt": null,
                              "status": "IN_PROGRESS",
                              "githubPullRequestReviewState": "PENDING_REVIEWER",
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": []
                            },
                            {
                              "uuid": null,
                              "type": "PULL_REQUEST",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 17259618,
                                "login": "alexbeno",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4"
                              },
                              "githubNumber": 1464,
                              "githubStatus": "DRAFT",
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-27T09:03:28Z",
                              "id": "d2924007831f961fa48d393013230781b0c6febeb9d83cf2f4dec2b13161e948",
                              "createdAt": "2023-11-27T09:03:28Z",
                              "completedAt": null,
                              "status": "IN_PROGRESS",
                              "githubPullRequestReviewState": "UNDER_REVIEW",
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 17259618,
                                "login": "alexbeno",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "links": []
                            },
                            {
                              "uuid": null,
                              "type": "CODE_REVIEW",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4"
                              },
                              "githubNumber": 1464,
                              "githubStatus": "COMMENTED",
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-27T09:03:28Z",
                              "id": "ec767f4a956861cc87245120dfa39b4167ff07383087541487617a8de306f945",
                              "createdAt": "2023-11-27T09:03:28Z",
                              "completedAt": null,
                              "status": "IN_PROGRESS",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                                "isRegistered": true,
                                "id": null
                              },
                              "links": [
                                {
                                  "uuid": null,
                                  "type": "PULL_REQUEST",
                                  "repo": {
                                    "id": 498695724,
                                    "owner": "onlydustxyz",
                                    "name": "marketplace-frontend",
                                    "description": null,
                                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                  },
                                  "githubAuthor": {
                                    "githubUserId": 17259618,
                                    "login": "alexbeno",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4"
                                  },
                                  "githubNumber": 1464,
                                  "githubStatus": "DRAFT",
                                  "githubTitle": "E 616 enhance panels stacking",
                                  "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                                  "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                                  "githubLabels": null,
                                  "lastUpdatedAt": null,
                                  "is_mine": false
                                }
                              ]
                            },
                            {
                              "uuid": null,
                              "type": "CODE_REVIEW",
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "githubAuthor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4"
                              },
                              "githubNumber": 1464,
                              "githubStatus": "COMMENTED",
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-11-27T09:03:28Z",
                              "id": "7361846a37de9b41029875d07c49299e3e816d65759bfb69c844055169df7ff8",
                              "createdAt": "2023-11-27T09:03:28Z",
                              "completedAt": null,
                              "status": "IN_PROGRESS",
                              "githubPullRequestReviewState": null,
                              "rewardIds": [],
                              "project": {
                                "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                "slug": "kaaper",
                                "name": "kaaper",
                                "logoUrl": null,
                                "shortDescription": "Documentation generator for Cairo projects.",
                                "visibility": "PUBLIC",
                                "languages": []
                              },
                              "contributor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                "isRegistered": false,
                                "id": null
                              },
                              "links": [
                                {
                                  "uuid": null,
                                  "type": "PULL_REQUEST",
                                  "repo": {
                                    "id": 498695724,
                                    "owner": "onlydustxyz",
                                    "name": "marketplace-frontend",
                                    "description": null,
                                    "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                  },
                                  "githubAuthor": {
                                    "githubUserId": 17259618,
                                    "login": "alexbeno",
                                    "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4"
                                  },
                                  "githubNumber": 1464,
                                  "githubStatus": "DRAFT",
                                  "githubTitle": "E 616 enhance panels stacking",
                                  "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                                  "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                                  "githubLabels": null,
                                  "lastUpdatedAt": null,
                                  "is_mine": false
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_churned_contributors() {
        // Given
        final String jwt = userAuthHelper.authenticateAntho().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_INSIGHTS_CHURNED_CONTRIBUTORS.formatted(KAAPER)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 4,
                          "totalItemNumber": 5,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributors": [
                            {
                              "githubUserId": 5160414,
                              "login": "haydencleary",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                              "isRegistered": true,
                              "id": null,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "1e65192b16f1b8550d9f11314f25c4610b62252c40336e1d74c5792002f110af",
                                "completedAt": "2023-12-04T14:12:52Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            },
                            {
                              "githubUserId": 17259618,
                              "login": "alexbeno",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                              "isRegistered": false,
                              "id": null,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "2971ac0b29502637dd945ce2b330c52440959ecbf95fca43a8eabf40e6d72860",
                                "completedAt": "2023-12-04T14:06:49Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            },
                            {
                              "githubUserId": 31901905,
                              "login": "kaelsky",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                              "isRegistered": true,
                              "id": null,
                              "cover": "CYAN",
                              "lastContribution": {
                                "id": "a3c85bd5e85f505766c62b67802fd2ddc450e2532924bef082af17e43a388c88",
                                "completedAt": "2023-12-04T13:44:50Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            },
                            {
                              "githubUserId": 143011364,
                              "login": "pixelfact",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                              "isRegistered": false,
                              "id": null,
                              "cover": "MAGENTA",
                              "lastContribution": {
                                "id": "9c4b19c7131b83e7c6aa7d7382d55a929715fcab67a56f19100e90427450db32",
                                "completedAt": "2023-12-04T10:33:39Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            },
                            {
                              "githubUserId": 10922658,
                              "login": "alexbensimon",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                              "isRegistered": true,
                              "id": null,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "61e77bb3d391ed1d9e93e586ed8cbb569352678a0c6eac0a8e43a007a960f149",
                                "completedAt": "2023-10-19T09:27:18Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            }
                          ]
                        }
                        """);
    }

    @SneakyThrows
    @Test
    void should_get_project_newcomers() {
        // Given
        final String jwt = userAuthHelper.authenticateAntho().jwt();

        try {
            patchPullRequestContributionsForNewcomer(498695724, List.of(1459L, 1497L, 1500L), ZonedDateTime.now().minusDays(1)); // A newcomer
            patchPullRequestContributionsForNewcomer(498695724, List.of(2L), ZonedDateTime.now().minusDays(29)); // Still a newcomer
            patchPullRequestContributionsForNewcomer(498695724, List.of(37L), ZonedDateTime.now().minusDays(31)); // No longer a newcomer

            // When
            client.get()
                    .uri(getApiURI(PROJECTS_INSIGHTS_NEWCOMERS.formatted(KAAPER)))
                    .header("Authorization", BEARER_PREFIX + jwt)
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
                              "contributors": [
                                {
                                  "githubUserId": 143011364,
                                  "login": "pixelfact",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                  "isRegistered": false,
                                  "id": null,
                                  "cover": "MAGENTA",
                                  "location": null,
                                  "bio": "Frontend Developer, API Enthusiast",
                                  "firstContributedAt": "2024-10-17T14:05:46.811962Z"
                                }
                              ]
                            }
                            """);
        } finally {
            restoreIndexerDump();
        }
    }

    @Test
    void should_get_project_most_active_contributors() {
        // Given
        final String jwt = userAuthHelper.authenticateAntho().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_INSIGHTS_MOST_ACTIVE_CONTRIBUTORS.formatted(KAAPER), Map.of("pageSize", "10")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributors[?(@.contributionCountPerWeeks.length() == 10)]").exists()
                .jsonPath("$.contributors[?(@.contributionCountPerWeeks.length() != 10)]").doesNotExist()
                .json("""
                        {
                           "contributors": [
                             {
                               "githubUserId": 43467246,
                               "login": "AnthonyBuisset",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 504,
                               "completedIssueCount": 11,
                               "completedCodeReviewCount": 388
                             },
                             {
                               "githubUserId": 595505,
                               "login": "ofux",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 248,
                               "completedIssueCount": 2,
                               "completedCodeReviewCount": 321
                             },
                             {
                               "githubUserId": 4435377,
                               "login": "Bernardstanislas",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 116,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 261
                             },
                             {
                               "githubUserId": 21149076,
                               "login": "oscarwroche",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 117,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 96
                             },
                             {
                               "githubUserId": 16590657,
                               "login": "PierreOucif",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 30,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 117
                             },
                             {
                               "githubUserId": 34384633,
                               "login": "tdelabro",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 107,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 39
                             },
                             {
                               "githubUserId": 5160414,
                               "login": "haydencleary",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 57,
                               "completedIssueCount": 1,
                               "completedCodeReviewCount": 82
                             },
                             {
                               "githubUserId": 31901905,
                               "login": "kaelsky",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 88,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 39
                             },
                             {
                               "githubUserId": 17259618,
                               "login": "alexbeno",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 78,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 26
                             },
                             {
                               "githubUserId": 143011364,
                               "login": "pixelfact",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 55,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 47
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 3,
                           "totalItemNumber": 10,
                           "nextPageIndex": 1
                         }
                        """);
    }

    private void patchPullRequestContributionsForNewcomer(long githubRepoId, List<Long> prNumbers, ZonedDateTime createdAt) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        UPDATE indexer_exp.contributions
                        SET created_at = :createdAt
                        WHERE
                            repo_id = :repoId AND
                            github_number in :prNumbers AND
                            type = 'PULL_REQUEST'
                        """)
                .setParameter("createdAt", createdAt)
                .setParameter("repoId", githubRepoId)
                .setParameter("prNumbers", prNumbers)
                .executeUpdate();

        em.createNativeQuery("""
                        DELETE FROM indexer_exp.contributions
                        WHERE
                            repo_id = :repoId AND
                            ((github_number not in :prNumbers AND type = 'PULL_REQUEST') OR type != 'PULL_REQUEST') AND
                            contributor_id IN (
                                SELECT contributor_id
                                FROM indexer_exp.contributions
                                WHERE repo_id = :repoId AND github_number in :prNumbers AND type = 'PULL_REQUEST'
                            )
                        """)
                .setParameter("repoId", githubRepoId)
                .setParameter("prNumbers", prNumbers)
                .executeUpdate();

        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
