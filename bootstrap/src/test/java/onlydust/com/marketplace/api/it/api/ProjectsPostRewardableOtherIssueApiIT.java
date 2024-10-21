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
public class ProjectsPostRewardableOtherIssueApiIT extends AbstractMarketplaceApiIT {


    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @Test
    @Order(1)
    public void should_be_unauthorized() {
        // When
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_ISSUE, UUID.randomUUID()))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubIssueHtmlUrl": "https://github.com/golang/go/issues/64179"
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
        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_ISSUE, projectId))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubIssueHtmlUrl": "https://github.com/golang/go/issues/64179"
                        }
                        """).header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange().expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Only project leads can add other issues as rewardable items");
    }

    @Test
    void should_create_and_close_rewardable_issue_given_a_project_lead_and_linked_repo() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        indexerApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/repos/onlydustxyz/starklings/issues/139"))
                .willReturn(ok()));

        client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_ISSUE, projectId))).contentType(APPLICATION_JSON).bodyValue("""
                        {
                          "githubIssueHtmlUrl": "https://github.com/onlydustxyz/starklings/issues/139"
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
                          "number": 139,
                          "id": "1243347024",
                          "contributionId": null,
                          "title": "Exercise on the common math operations",
                          "createdAt": "2022-05-20T15:56:33Z",
                          "completedAt": null,
                          "githubBody": "## Description\\r\\nThe common library contains predefined functions to handle common math operations. \\r\\nSuch as `<`, `<=`, `> 0`, range checks, absolute value, felt division.\\r\\nProvide an exercise to learn how to use those functions.\\r\\n\\r\\nResource:\\r\\n - https://perama-v.github.io/cairo/examples/math/\\r\\n\\r\\n## Acceptance criteria\\r\\n - The exercise showcase math common library functions\\r\\n - The contract compiles and tests pass\\r\\n - An exercise patch is generated",
                          "author": {
                            "githubUserId": 98529704,
                            "login": "tekkac",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4"
                          },
                          "repoName": "starklings",
                          "repoId": 480776993,
                          "type": "ISSUE",
                          "commitsCount": null,
                          "userCommitsCount": null,
                          "commentsCount": 1,
                          "status": "OPEN",
                          "ignored": false,
                          "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/139"
                        }
                        """);
    }
}
