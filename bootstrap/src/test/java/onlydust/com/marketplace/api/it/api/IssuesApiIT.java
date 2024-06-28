package onlydust.com.marketplace.api.it.api;

import org.junit.jupiter.api.Test;

public class IssuesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_issue_by_id() {
        final var olivier = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(getApiURI(String.format(ISSUES_BY_ID, "1736474921")))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": 1736474921,
                          "number": 1111,
                          "title": "Documentation by AnthonyBuisset",
                          "status": "OPEN",
                          "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111",
                          "githubAppInstallationStatus": null,
                          "githubAppInstallationPermissionsUpdateUrl": null
                        }
                        """);
    }


    @Test
    void should_return_issue_by_id_as_project_lead() {
        final var olivier = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(getApiURI(String.format(ISSUES_BY_ID, "1835403768")))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": 1835403768,
                          "number": 6,
                          "title": "Test #2",
                          "status": "OPEN",
                          "htmlUrl": "https://github.com/onlydustxyz/od-rust-template/issues/6",
                          "githubAppInstallationStatus": "MISSING_PERMISSIONS",
                          "githubAppInstallationPermissionsUpdateUrl": "https://github.com/organizations/onlydustxyz/settings/installations/44741576/permissions/update"
                        }
                        """);
    }
}
