package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.suites.tags.TagReward;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@TagReward
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
        userAuthHelper.signUpUser(1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = userAuthHelper.authenticateUser(1L).jwt();
        final var projectId = projectRepository.findAll().get(0).getId();

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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        indexerApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/repos/tokio-rs/tracing/pull-requests/2820"))
                .willReturn(ok()));

        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_PR, projectId))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubPullRequestHtmlUrl": "https://github.com/tokio-rs/tracing/pull/2820"
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
                          "number": 2820,
                          "id": "1621758565",
                          "contributionId": null,
                          "title": "tracing: add index API for `Field`",
                          "createdAt": "2023-11-29T12:36:19Z",
                          "completedAt": null,
                          "githubBody": "<!--\\r\\nThank you for your Pull Request. Please provide a description above and review\\r\\nthe requirements below.\\r\\n\\r\\nBug fixes and new features should include tests.\\r\\n\\r\\nContributors guide: https://github.com/tokio-rs/tracing/blob/master/CONTRIBUTING.md\\r\\n-->\\r\\n\\r\\n## Motivation\\r\\n\\r\\nExpose the index of the field. See more at: https://github.com/tokio-rs/console/issues/462#issuecomment-1830842319\\r\\n\\r\\n## Solution\\r\\n\\r\\nJust added a new API.",
                          "author": {
                            "githubUserId": 29879298,
                            "login": "hi-rustin",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/29879298?v=4"
                          },
                          "repoName": "tracing",
                          "repoId": 165289175,
                          "type": "PULL_REQUEST",
                          "commitsCount": 2,
                          "userCommitsCount": null,
                          "commentsCount": null,
                          "status": "OPEN",
                          "ignored": false,
                          "htmlUrl": "https://github.com/tokio-rs/tracing/pull/2820"
                        }
                        """);
    }
}
