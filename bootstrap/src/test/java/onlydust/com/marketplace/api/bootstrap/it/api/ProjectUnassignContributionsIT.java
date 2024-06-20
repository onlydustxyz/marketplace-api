package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectUnassignContributionsIT extends AbstractMarketplaceApiIT {

    @Test
    void should_unassign_contribution() {
        // Given
        final var projectId = UUID.fromString("61076487-6ec5-4751-ab0d-3b876c832239");
        final var contributionId = "295c20a54bfdd199139d4bd05ff0d8fcf8bdb1e7207ed3551908a86373947fa1";
        final var projectLead = userAuthHelper.authenticateOlivier();

        githubWireMockServer.stubFor(post(urlEqualTo("/app/installations/44741576/access_tokens"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("""
                                {
                                    "token": "GITHUB_APP_PERSONAL_ACCESS_TOKEN",
                                    "permissions": {
                                        "issues": "write"
                                    }
                                }
                                """)
                ));

        githubWireMockServer.stubFor(delete(urlEqualTo("/repositories/663102799/issues/4/assignees"))
                .withHeader("Authorization", matching("Bearer GITHUB_APP_PERSONAL_ACCESS_TOKEN"))
                .withRequestBody(equalToJson("""
                        {
                          "assignees" : [ "PierreOucif" ]
                        }
                        """))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 1974125983
                                }
                                """)
                ));

        client.post()
                .uri(getApiURI(PROJECT_CONTRIBUTION_UNASSIGN.formatted(projectId, contributionId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        githubWireMockServer.verify(deleteRequestedFor(urlEqualTo("/repositories/663102799/issues/4/assignees")));
    }
}
