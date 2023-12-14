package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
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
                              "githubUserId": 21149076,
                              "login": "oscarwroche",
                              "htmlUrl": "https://github.com/oscarwroche",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                              "isRegistered": true,
                              "cover": "BLUE",
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
                            },
                            {
                              "githubUserId": 4435377,
                              "login": "Bernardstanislas",
                              "htmlUrl": "https://github.com/Bernardstanislas",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                              "isRegistered": true,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "9acd1978a8a8d714edbfe888fe9fa5a053f80b7aa7e283023a852a7a2153474a",
                                "completedAt": "2023-05-12T15:24:46Z",
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
                              "githubUserId": 129528947,
                              "login": "VeryDustyBot",
                              "htmlUrl": "https://github.com/VeryDustyBot",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                              "isRegistered": false,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "567e9f0026852b91ccd0e790cb408af83be7c6517df88fbeabf46f97e636b2ca",
                                "completedAt": "2023-03-31T15:54:26Z",
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
                              "githubUserId": 112474158,
                              "login": "onlydust-contributor",
                              "htmlUrl": "https://github.com/onlydust-contributor",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                              "isRegistered": true,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "b100aafc326213a7e0066918abedda38e14fda8b68331e7d777189eae7d41951",
                                "completedAt": "2023-01-05T08:31:31Z",
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
                              "githubUserId": 63618058,
                              "login": "0xAurelou",
                              "htmlUrl": "https://github.com/0xAurelou",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/63618058?v=4",
                              "isRegistered": false,
                              "cover": "BLUE",
                              "lastContribution": {
                                "id": "55adb08b558ed5f94541b1ba28e6e5b721aeb29f2afbf23e4065be4f2fdff914",
                                "completedAt": "2022-11-07T15:36:51Z",
                                "repo": {
                                  "id": 493591124,
                                  "owner": "onlydustxyz",
                                  "name": "kaaper",
                                  "description": "Documentation generator for Cairo projects.",
                                  "htmlUrl": "https://github.com/onlydustxyz/kaaper"
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
                              "cover": "BLUE",
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
                              "cover": "BLUE",
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
                              "cover": "BLUE",
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
}
