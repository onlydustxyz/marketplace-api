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
                          "repo": {
                            "id": 602953640,
                            "name": "cool.repo.B",
                            "description": null,
                            "htmlUrl": "https://github.com/od-mocks/cool.repo.B"
                          },
                          "author": {
                            "githubUserId": 112474158,
                            "login": "onlydust-contributor",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                            "isRegistered": true
                          },
                          "createdAt": "2023-06-01T14:39:49Z",
                          "closedAt": null,
                          "body": "Real cool documentation",
                          "commentCount": 0,
                          "labels": [],
                          "applicants": [],
                          "languages": [],
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
                          "repo": {
                            "id": 663102799,
                            "name": "od-rust-template",
                            "description": null,
                            "htmlUrl": "https://github.com/onlydustxyz/od-rust-template"
                          },
                          "author": {
                            "githubUserId": 16590657,
                            "login": "PierreOucif",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                            "isRegistered": true
                          },
                          "createdAt": "2023-08-03T16:46:40Z",
                          "closedAt": null,
                          "body": "aaaaaa",
                          "commentCount": 0,
                          "labels": [],
                          "applicants": [],
                          "languages": [],
                          "githubAppInstallationStatus": "MISSING_PERMISSIONS",
                          "githubAppInstallationPermissionsUpdateUrl": "https://github.com/organizations/onlydustxyz/settings/installations/44741576/permissions/update"
                        }
                        """);
    }
}
