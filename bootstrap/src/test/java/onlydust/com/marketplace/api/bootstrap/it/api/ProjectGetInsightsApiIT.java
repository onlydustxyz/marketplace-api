package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.UserContributionStats;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import org.assertj.core.internal.IntArrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectGetInsightsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    HasuraUserHelper userHelper;

    private final static String KAAPER = "298a547f-ecb6-4ab2-8975-68f4e9bf7b39";

    @Test
    void should_get_staled_contributions() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

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
                                "htmlUrl": "https://github.com/pixelfact",
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
                                "htmlUrl": "https://github.com/haydencleary",
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
                                "htmlUrl": "https://github.com/pixelfact",
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
                                "htmlUrl": "https://github.com/alexbeno",
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
                                "htmlUrl": "https://github.com/haydencleary",
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
        final String jwt = userHelper.authenticateAnthony().jwt();

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
                              "htmlUrl": "https://github.com/kaelsky",
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
                              "htmlUrl": "https://github.com/pixelfact",
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
                              "htmlUrl": "https://github.com/haydencleary",
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
                              "htmlUrl": "https://github.com/alexbeno",
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
                              "htmlUrl": "https://github.com/oscarwroche",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                              "isRegistered": true,
                              "cover": "CYAN",
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

    @Test
    void should_get_project_newcomers() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

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
                               "githubUserId": 17259618,
                               "login": "alexbeno",
                               "htmlUrl": "https://github.com/alexbeno",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                               "isRegistered": false,
                               "cover": "BLUE",
                               "location": null,
                               "bio": null,
                               "firstContributedAt": "2023-10-19T12:13:47Z"
                             },
                             {
                               "githubUserId": 143011364,
                               "login": "pixelfact",
                               "htmlUrl": "https://github.com/pixelfact",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                               "isRegistered": false,
                               "cover": "MAGENTA",
                               "location": null,
                               "bio": "Frontend Dev",
                               "firstContributedAt": "2023-09-25T15:49:00Z"
                             },
                             {
                               "githubUserId": 5160414,
                               "login": "haydencleary",
                               "htmlUrl": "https://github.com/haydencleary",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                               "isRegistered": true,
                               "cover": "BLUE",
                               "location": "Limoges, France",
                               "bio": "Freelance web developer focused on Typescript and React.js",
                               "firstContributedAt": "2023-09-18T14:41:40Z"
                             },
                             {
                               "githubUserId": 31901905,
                               "login": "kaelsky",
                               "htmlUrl": "https://github.com/kaelsky",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                               "isRegistered": true,
                               "cover": "CYAN",
                               "location": null,
                               "bio": null,
                               "firstContributedAt": "2023-09-05T09:12:08Z"
                             },
                             {
                               "githubUserId": 16590657,
                               "login": "PierreOucif",
                               "htmlUrl": "https://github.com/PierreOucif",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                               "isRegistered": true,
                               "cover": "CYAN",
                               "location": "Paris",
                               "bio": "Je me lève très tôt et mange à midi pile, n'en déplaise aux grincheux",
                               "firstContributedAt": "2023-07-12T11:54:35Z"
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 5,
                           "totalItemNumber": 5,
                           "nextPageIndex": 1
                         }
                        """);
    }


    @Test
    void should_get_project_most_active_contributors() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

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
                               "htmlUrl": "https://github.com/AnthonyBuisset",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 504,
                               "completedIssueCount": 11,
                               "completedCodeReviewCount": 388,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 595505,
                               "login": "ofux",
                               "htmlUrl": "https://github.com/ofux",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 248,
                               "completedIssueCount": 2,
                               "completedCodeReviewCount": 321,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 4435377,
                               "login": "Bernardstanislas",
                               "htmlUrl": "https://github.com/Bernardstanislas",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 116,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 261,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 21149076,
                               "login": "oscarwroche",
                               "htmlUrl": "https://github.com/oscarwroche",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 117,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 96,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 16590657,
                               "login": "PierreOucif",
                               "htmlUrl": "https://github.com/PierreOucif",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 30,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 117,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 1,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 12,
                                   "issueCount": 0,
                                   "pullRequestCount": 1
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 34384633,
                               "login": "tdelabro",
                               "htmlUrl": "https://github.com/tdelabro",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 107,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 39,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 5160414,
                               "login": "haydencleary",
                               "htmlUrl": "https://github.com/haydencleary",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 57,
                               "completedIssueCount": 1,
                               "completedCodeReviewCount": 82,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 1,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 9,
                                   "issueCount": 0,
                                   "pullRequestCount": 14
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 31901905,
                               "login": "kaelsky",
                               "htmlUrl": "https://github.com/kaelsky",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                               "isRegistered": true,
                               "completedPullRequestCount": 88,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 39,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 10,
                                   "issueCount": 0,
                                   "pullRequestCount": 2
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 17259618,
                               "login": "alexbeno",
                               "htmlUrl": "https://github.com/alexbeno",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 78,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 26,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 2
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 10,
                                   "issueCount": 0,
                                   "pullRequestCount": 21
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             },
                             {
                               "githubUserId": 143011364,
                               "login": "pixelfact",
                               "htmlUrl": "https://github.com/pixelfact",
                               "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                               "isRegistered": false,
                               "completedPullRequestCount": 55,
                               "completedIssueCount": 0,
                               "completedCodeReviewCount": 47,
                               "contributionCountPerWeeks": [
                                 {
                                   "year": 2024,
                                   "week": 1,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 52,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 51,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 50,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 49,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 48,
                                   "codeReviewCount": 8,
                                   "issueCount": 0,
                                   "pullRequestCount": 14
                                 },
                                 {
                                   "year": 2023,
                                   "week": 47,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 46,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 45,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 },
                                 {
                                   "year": 2023,
                                   "week": 44,
                                   "codeReviewCount": 0,
                                   "issueCount": 0,
                                   "pullRequestCount": 0
                                 }
                               ]
                             }
                           ],
                           "hasMore": true,
                           "totalPageNumber": 3,
                           "totalItemNumber": 10,
                           "nextPageIndex": 1
                         }
                        """);
    }
}
