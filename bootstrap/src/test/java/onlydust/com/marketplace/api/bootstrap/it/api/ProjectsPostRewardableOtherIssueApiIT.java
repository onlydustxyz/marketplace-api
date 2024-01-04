package onlydust.com.marketplace.api.bootstrap.it.api;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsPostRewardableOtherIssueApiIT extends AbstractMarketplaceApiIT {

  @Autowired
  HasuraUserHelper hasuraUserHelper;
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
    hasuraUserHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
        false);
    final String jwt = hasuraUserHelper.authenticateUser(1L).jwt();
    final UUID projectId = projectRepository.findAll().get(0).getId();

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
    final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
    final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

    // When
    indexerApiWireMockServer.stubFor(put(urlEqualTo("/api/v1/repos/onlydustxyz/starklings/issues/100"))
        .willReturn(ok()));

    client.post().uri(getApiURI(String.format(PROJECTS_POST_REWARDABLE_OTHER_ISSUE, projectId))).contentType(APPLICATION_JSON).bodyValue("""
            {
              "githubIssueHtmlUrl": "https://github.com/onlydustxyz/starklings/issues/100"
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
              "number": 100,
              "id": "1231618572",
              "contributionId": null,
              "title": "feature: soluce command in the CLI",
              "createdAt": "2022-05-10T19:49:36Z",
              "completedAt": "2022-05-12T14:10:19Z",
              "repoName": "starklings",
              "repoId": 480776993,
              "type": "ISSUE",
              "commitsCount": null,
              "userCommitsCount": null,
              "commentsCount": 2,
              "status": "COMPLETED",
              "ignored": false,
              "htmlUrl": "https://github.com/onlydustxyz/starklings/issues/100"
            }
                                    
            """);
  }
}
