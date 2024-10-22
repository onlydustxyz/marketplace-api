package onlydust.com.marketplace.api.it.api.feature;

import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.ArchivedGithubContributionRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TagProject
public class UpdateContributionsIT extends AbstractMarketplaceApiIT {

    @Autowired
    ArchivedGithubContributionRepository archivedGithubContributionRepository;

    @Test
    void should_archive_unarchive_issue() {
        // Given
        final var issueId = ContributionUUID.of(2013944953L);

        // When
        client.patch()
                .uri(getApiURI(String.format(V2_ISSUES_BY_ID, issueId)))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .bodyValue("""
                        {
                          "archived": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(archivedGithubContributionRepository.findById(issueId.value()).isPresent());

        // When
        client.patch()
                .uri(getApiURI(String.format(V2_ISSUES_BY_ID, issueId)))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .bodyValue("""
                        {
                          "archived": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(archivedGithubContributionRepository.findById(issueId.value()).isEmpty());
    }

    @Test
    void should_archive_unarchive_pull_request() {
        // Given
        final var prId = ContributionUUID.of(1623877895L);

        // When
        client.patch()
                .uri(getApiURI(String.format(PULL_REQUESTS_BY_ID, prId)))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .bodyValue("""
                        {
                          "archived": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(archivedGithubContributionRepository.findById(prId.value()).isPresent());

        // When
        client.patch()
                .uri(getApiURI(String.format(PULL_REQUESTS_BY_ID, prId)))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .bodyValue("""
                        {
                          "archived": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertTrue(archivedGithubContributionRepository.findById(prId.value()).isEmpty());
    }

    @Test
    void should_close_issue() {
        // Given
        final var contributionUUID = ContributionUUID.of(2013944953L);

        // When
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

        client.patch()
                .uri(getApiURI(String.format(V2_ISSUES_BY_ID, contributionUUID)))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .bodyValue("""
                        {
                          "archived": null,
                          "closed": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        githubWireMockServer.verify(postRequestedFor(urlEqualTo("/repositories/498695724/issues/1469"))
                .withHeader("Authorization", equalTo("Bearer GITHUB_APP_PERSONAL_ACCESS_TOKEN"))
                .withRequestBody(equalToJson("""
                        {
                            "state": "closed"
                        }
                        """)));
    }
}
