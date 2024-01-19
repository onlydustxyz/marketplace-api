package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsPostRewardableOtherWorkApiIT extends AbstractMarketplaceApiIT {

    private static final String CREATE_ISSUE_RESPONSE_JSON = """
            {
                                  "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25",
                                  "repository_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend",
                                  "labels_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/labels{/name}",
                                  "comments_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/comments",
                                  "events_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/events",
                                  "html_url": "https://github.com/onlydustxyz/marketplace-frontend/issues/25",
                                  "id": 1840630179,
                                  "node_id": "I_kwDOJ4YlT85ttcmj",
                                  "number": 25,
                                  "title": "%s",
                                  "user": {
                                    "login": "PierreOucif",
                                    "id": 16590657,
                                    "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                                    "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                    "gravatar_id": "",
                                    "url": "https://api.github.com/users/PierreOucif",
                                    "html_url": "https://github.com/PierreOucif",
                                    "followers_url": "https://api.github.com/users/PierreOucif/followers",
                                    "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                                    "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                                    "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                                    "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                                    "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                                    "repos_url": "https://api.github.com/users/PierreOucif/repos",
                                    "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                                    "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                                    "type": "User",
                                    "site_admin": false
                                  },
                                  "labels": [],
                                  "state": "open",
                                  "locked": false,
                                  "assignee": null,
                                  "assignees": [],
                                  "milestone": null,
                                  "comments": 0,
                                  "created_at": "2023-08-08T06:11:35Z",
                                  "updated_at": "2023-08-08T06:11:35Z",
                                  "closed_at": null,
                                  "author_association": "MEMBER",
                                  "active_lock_reason": null,
                                  "body": null,
                                  "closed_by": null,
                                  "reactions": {
                                    "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/reactions",
                                    "total_count": 0,
                                    "+1": 0,
                                    "-1": 0,
                                    "laugh": 0,
                                    "hooray": 0,
                                    "confused": 0,
                                    "heart": 0,
                                    "rocket": 0,
                                    "eyes": 0
                                  },
                                  "timeline_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/timeline",
                                  "performed_via_github_app": null,
                                  "state_reason": null
                                }""";
    private static final String CLOSE_ISSUE_RESPONSE_JSON = """
            {
              "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25",
              "repository_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend",
              "labels_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/labels{/name}",
              "comments_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/comments",
              "events_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/events",
              "html_url": "https://github.com/onlydustxyz/marketplace-frontend/issues/25",
              "id": 1840630179,
              "node_id": "I_kwDOJ4YlT85ttcmj",
              "number": 25,
              "title": "%s",
              "user": {
                "login": "PierreOucif",
                "id": 16590657,
                "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/PierreOucif",
                "html_url": "https://github.com/PierreOucif",
                "followers_url": "https://api.github.com/users/PierreOucif/followers",
                "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                "repos_url": "https://api.github.com/users/PierreOucif/repos",
                "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                "type": "User",
                "site_admin": false
              },
              "labels": [],
              "state": "closed",
              "locked": false,
              "assignee": null,
              "assignees": [],
              "milestone": null,
              "comments": 0,
              "created_at": "2023-08-08T06:11:35Z",
              "updated_at": "2023-08-08T06:13:08Z",
              "closed_at": "2023-08-08T06:13:08Z",
              "author_association": "MEMBER",
              "active_lock_reason": null,
              "body": "This a body",
              "closed_by": {
                "login": "PierreOucif",
                "id": 16590657,
                "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                "gravatar_id": "",
                "url": "https://api.github.com/users/PierreOucif",
                "html_url": "https://github.com/PierreOucif",
                "followers_url": "https://api.github.com/users/PierreOucif/followers",
                "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                "repos_url": "https://api.github.com/users/PierreOucif/repos",
                "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                "type": "User",
                "site_admin": false
              },
              "reactions": {
                "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/reactions",
                "total_count": 0,
                "+1": 0,
                "-1": 0,
                "laugh": 0,
                "hooray": 0,
                "confused": 0,
                "heart": 0,
                "rocket": 0,
                "eyes": 0
              },
              "timeline_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/timeline",
              "performed_via_github_app": null,
              "state_reason": "completed"
            }

                """;

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    GithubHttpClient.Config githubDustyBotConfig;

    @Test
    @Order(1)
    public void should_be_unauthorized() {
        // When
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_WORK, UUID.randomUUID()))).contentType(APPLICATION_JSON).bodyValue(String.format("""
                        {
                          "githubRepoId": 1,
                          "title": "test",
                          "description": "test"
                        }
                        """, UUID.randomUUID()))
                // Then
                .exchange().expectStatus().isEqualTo(401);
    }

    @Test
    @Order(2)
    void should_be_forbidden_given_authenticated_user_not_project_lead() {
        // Given
        userAuthHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();

        // When
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_WORK, UUID.randomUUID()))).contentType(APPLICATION_JSON).bodyValue(String.format("""
                        {
                          "githubRepoId": 1,
                          "title": "test",
                          "description": "test"
                        }
                        """, projectId)).header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange().expectStatus().isEqualTo(403).expectBody().jsonPath("$.message").isEqualTo("Only project "
                                                                                                       + "leads can " + "create " + "rewardable " + "issue on" +
                                                                                                       " " + "their " + "projects");
    }

    @Test
    @Order(3)
    void should_be_forbidden_given_authenticated_user_project_lead_on_not_linked_repo() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_WORK, projectId))).contentType(APPLICATION_JSON).bodyValue(String.format("""
                        {
                          "githubRepoId": 554756922,
                          "title": "test",
                          "description": "test"
                        }
                        """, projectId)).header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange().expectStatus().isEqualTo(403).expectBody().jsonPath("$.message").isEqualTo("Rewardable " + "issue can " + "only be " + "created " +
                                                                                                       "on " + "repos linked " + "to this " + "project");
    }

    @Test
    void should_create_and_close_rewardable_issue_given_a_project_lead_and_linked_repo() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final Long repoId = 498695724L;
        final String repoName = "marketplace-frontend";
        final String owner = "onlydustxyz";
        final String title = faker.backToTheFuture().quote();
        final String description = faker.lorem().paragraph();

        // When
        dustyBotApiWireMockServer.stubFor(post(urlEqualTo(String.format("/repos/%s/%s/issues", owner, repoName)))
                .withHeader("Authorization", equalTo("Bearer " + githubDustyBotConfig.getPersonalAccessToken()))
                .withRequestBody(equalToJson(String.format("""
                        {
                            "body": "%s",
                            "title": "%s"
                        }
                        """, description, title)))
                .willReturn(okJson(CREATE_ISSUE_RESPONSE_JSON)));

        dustyBotApiWireMockServer.stubFor(post(urlEqualTo(String.format("/repos/%s/%s/issues/%s", owner, repoName, 25)))
                .withHeader("Authorization", equalTo("Bearer " + githubDustyBotConfig.getPersonalAccessToken()))
                .withRequestBody(equalToJson("""
                        {
                            "state": "closed"
                        }
                        """))
                .willReturn(okJson(String.format(CLOSE_ISSUE_RESPONSE_JSON, title))));

        indexerApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/repos/%s/%s/issues/%s".formatted(owner, repoName, 25)))
                .willReturn(ok()));

        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_WORK, projectId)))
                .contentType(APPLICATION_JSON)
                .bodyValue(String.format("""
                        {
                          "githubRepoId": %s,
                          "title": "%s",
                          "description": "%s"
                        }
                        """, repoId, title, description))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                           "number": 25,
                           "id": "1840630179",
                           "contributionId": null,
                           "title": "%s",
                           "createdAt": "2023-08-08T06:11:35Z",
                           "completedAt": "2023-08-08T06:13:08Z",
                           "githubBody": "This a body",
                           "author": {
                             "githubUserId": 16590657,
                             "login": "PierreOucif",
                             "htmlUrl": "https://github.com/PierreOucif",
                             "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                           },
                           "repoName": "marketplace-frontend",
                           "repoId": 498695724,
                           "type": "ISSUE",
                           "commitsCount": null,
                           "userCommitsCount": null,
                           "commentsCount": 0,
                           "status": "CLOSED",
                           "ignored": false,
                           "htmlUrl": "https://github.com/onlydustxyz/marketplace-frontend/issues/25"
                         }
                        """.formatted(title));
    }
}
