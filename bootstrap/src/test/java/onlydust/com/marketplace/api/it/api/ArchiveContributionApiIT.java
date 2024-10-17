package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.repository.ArchivedGithubContributionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArchiveContributionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ArchivedGithubContributionRepository archivedGithubContributionRepository;

    @Test
    void should_archive_unarchive_issue() {
        // Given
        final Long issueId = 2013944953L;

        // When
        client.patch()
                .uri(getApiURI(String.format(ISSUES_BY_ID, issueId)))
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

        assertTrue(archivedGithubContributionRepository.findById(issueId).isPresent());

        // When
        client.patch()
                .uri(getApiURI(String.format(ISSUES_BY_ID, issueId)))
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

        assertTrue(archivedGithubContributionRepository.findById(issueId).isEmpty());
    }

    @Test
    void should_archive_unarchive_pull_request() {
        // Given
        final Long prId = 953656792L;

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

        assertTrue(archivedGithubContributionRepository.findById(prId).isPresent());

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

        assertTrue(archivedGithubContributionRepository.findById(prId).isEmpty());
    }


}
