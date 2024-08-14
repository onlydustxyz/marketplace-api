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
                          "contributions": [
                            {
                              "id": "662217b394e92161dac7470d717508a9287bb253e9fc8d9206f750ee4b1a9f35",
                              "contributor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                "isRegistered": false
                              },
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "type": "PULL_REQUEST",
                              "status": "IN_PROGRESS",
                              "githubNumber": 1514,
                              "githubTitle": "Migrate Profile mutation",
                              "githubStatus": "DRAFT",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1514",
                              "githubBody": null,
                              "createdAt": "2023-12-01T16:53:55Z"
                            },
                            {
                              "id": "440ada773bbb1c112e039324edac9bd2e2dd612d2c7b99d9cc73aaa4ad483c11",
                              "contributor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                                "isRegistered": true
                              },
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "type": "PULL_REQUEST",
                              "status": "IN_PROGRESS",
                              "githubNumber": 1503,
                              "githubTitle": "B 1178 build new contributions tab",
                              "githubStatus": "DRAFT",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1503",
                              "githubBody": null,
                              "createdAt": "2023-11-30T16:32:31Z"
                            },
                            {
                              "id": "7361846a37de9b41029875d07c49299e3e816d65759bfb69c844055169df7ff8",
                              "contributor": {
                                "githubUserId": 143011364,
                                "login": "pixelfact",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                "isRegistered": false
                              },
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "type": "CODE_REVIEW",
                              "status": "IN_PROGRESS",
                              "githubNumber": 1464,
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubStatus": "COMMENTED",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "createdAt": "2023-11-27T09:03:28Z"
                            },
                            {
                              "id": "d2924007831f961fa48d393013230781b0c6febeb9d83cf2f4dec2b13161e948",
                              "contributor": {
                                "githubUserId": 17259618,
                                "login": "alexbeno",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                                "isRegistered": false
                              },
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "type": "PULL_REQUEST",
                              "status": "IN_PROGRESS",
                              "githubNumber": 1464,
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubStatus": "DRAFT",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "createdAt": "2023-11-27T09:03:28Z"
                            },
                            {
                              "id": "ec767f4a956861cc87245120dfa39b4167ff07383087541487617a8de306f945",
                              "contributor": {
                                "githubUserId": 5160414,
                                "login": "haydencleary",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                                "isRegistered": true
                              },
                              "repo": {
                                "id": 498695724,
                                "owner": "onlydustxyz",
                                "name": "marketplace-frontend",
                                "description": null,
                                "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                              },
                              "type": "CODE_REVIEW",
                              "status": "IN_PROGRESS",
                              "githubNumber": 1464,
                              "githubTitle": "E 616 enhance panels stacking",
                              "githubStatus": "COMMENTED",
                              "githubHtmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/pull/1464",
                              "githubBody": "`src/App/Stacks/Stacks.tsx` and `src/App/Stacks` will be deleted before merging\\r\\n\\r\\nI keep some commented code but it will be deleted before merging\\r\\n\\r\\n\\r\\nImplemented panel : \\r\\n- ContributorProfile\\r\\n- ProjectReward\\r\\n- MyReward\\r\\n- Contribution\\r\\n",
                              "createdAt": "2023-11-27T09:03:28Z"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 2,
                          "totalItemNumber": 7,
                          "nextPageIndex": 1
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
                          "contributors": [
                            {
                              "githubUserId": 31901905,
                              "login": "kaelsky",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                              "isRegistered": true,
                              "cover": "CYAN",
                              "lastContribution": {
                                "id": "48c6f61a076454ba2ce5d8c52fd21deccd50aeb7953dd22b174eb2384fdea479",
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
                              "githubUserId": 143011364,
                              "login": "pixelfact",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                              "isRegistered": false,
                              "cover": "MAGENTA",
                              "lastContribution": {
                                "id": "bb134dc807abcdc8125104ea79a76b3e4a99a04eb9e734fb2f17dc26f9ac65f8",
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
                              "githubUserId": 5160414,
                              "login": "haydencleary",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                              "isRegistered": true,
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
                              "githubUserId": 21149076,
                              "login": "oscarwroche",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                              "isRegistered": true,
                              "cover": "MAGENTA",
                              "lastContribution": {
                                "id": "39f0ee86f57fc94477f7525ca035800e54f79c7c20c8bc2774b916bcc514d921",
                                "completedAt": "2023-07-13T16:38:18Z",
                                "repo": {
                                  "id": 498695724,
                                  "owner": "onlydustxyz",
                                  "name": "marketplace-frontend",
                                  "description": "Contributions marketplace backend services",
                                  "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend"
                                }
                              }
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 5,
                          "totalItemNumber": 5,
                          "nextPageIndex": 1
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
                              "contributors": [
                                {
                                  "githubUserId": 143011364,
                                  "login": "pixelfact",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                                  "isRegistered": false,
                                  "cover": "MAGENTA",
                                  "location": null,
                                  "bio": "Frontend Dev"
                                },
                                {
                                  "githubUserId": 45264458,
                                  "login": "abdelhamidbakhta",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                                  "isRegistered": false,
                                  "cover": "BLUE",
                                  "location": "Genesis",
                                  "bio": "Starknet Exploration Lead\\r\\n.\\r\\nΞthereum Core Developer\\r\\n.\\r\\nΞIP-1559 Champion\\r\\n.\\r\\nBitcoin lover"
                                }
                              ],
                              "hasMore": false,
                              "totalPageNumber": 1,
                              "totalItemNumber": 2,
                              "nextPageIndex": 0
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
