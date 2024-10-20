package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.repository.ArchivedGithubContributionRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TagProject
public class ArchiveContributionApiIT extends AbstractMarketplaceApiIT {

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


}
