package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
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
public class ProjectsPostRewardableOtherPullRequestApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @Test
    @Order(1)
    public void should_be_unauthorized() {
        // When
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_PR, UUID.randomUUID()))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubPullRequestHtmlUrl": "https://github.com/golang/go/pulls/64179"
                        }
                        """)
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
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_PR, projectId))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubPullRequestHtmlUrl": "https://github.com/golang/go/pulls/64179"
                        }
                        """).header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange().expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Only project leads can add other pull requests as rewardable items");
    }

    @Test
    void should_create_and_close_rewardable_issue_given_a_project_lead_and_linked_repo() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        indexerApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/repos/tokio-rs/tracing/pull-requests/608"))
                .willReturn(ok()));

        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_PR, projectId))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubPullRequestHtmlUrl": "https://github.com/tokio-rs/tracing/pull/608"
                        }
                        """)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "number": 608,
                          "id": "381072085",
                          "contributionId": null,
                          "title": "Fix typo in example code",
                          "createdAt": "2020-02-27T21:03:44Z",
                          "completedAt": "2020-02-27T21:29:57Z",
                          "githubBody": "Hi, I noticed a small typo while checking out the README \\uD83D\\uDE42 ",
                          "author": {
                            "githubUserId": 7748404,
                            "login": "kvrhdn",
                            "htmlUrl": "https://github.com/kvrhdn",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/7748404?v=4"
                          },
                          "repoName": "tracing",
                          "repoId": 165289175,
                          "type": "PULL_REQUEST",
                          "commitsCount": 1,
                          "userCommitsCount": null,
                          "commentsCount": null,
                          "status": "MERGED",
                          "ignored": false,
                          "htmlUrl": "https://github.com/tokio-rs/tracing/pull/608"
                        }
                        """);
    }
}
