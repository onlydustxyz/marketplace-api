package onlydust.com.marketplace.api.it.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class IssuesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_issue_by_id() {
        final var olivier = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(getApiURI(String.format(ISSUES_BY_ID, "1952203217")))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": 1952203217,
                          "number": 4,
                          "title": "Test pierre 2",
                          "status": "COMPLETED",
                          "htmlUrl": "https://github.com/gregcha/bretzel-app/issues/4",
                          "repo": {
                            "id": 380954304,
                            "owner": "gregcha",
                            "name": "bretzel-app",
                            "description": null,
                            "htmlUrl": "https://github.com/gregcha/bretzel-app"
                          },
                          "author": {
                            "githubUserId": 136717950,
                            "login": "od-staging",
                            "avatarUrl": "https://avatars.githubusercontent.com/u/136717950?v=4",
                            "isRegistered": false,
                            "id": null
                          },
                          "createdAt": "2023-10-19T12:59:45Z",
                          "closedAt": "2023-10-19T12:59:46Z",
                          "body": "aaaa",
                          "commentCount": 0,
                          "labels": [],
                          "applicants": [],
                          "assignees": [],
                          "languages": [],
                          "githubAppInstallationStatus": null,
                          "githubAppInstallationPermissionsUpdateUrl": null
                        }
                        """);
    }

    @Autowired
    EntityManagerFactory entityManagerFactory;

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
                          "assignees": [],
                          "languages": [],
                          "githubAppInstallationStatus": "MISSING_PERMISSIONS",
                          "githubAppInstallationPermissionsUpdateUrl": "https://github.com/organizations/onlydustxyz/settings/installations/44741576/permissions/update"
                        }
                        """);

        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("update indexer_exp.github_accounts set type = 'USER' where id = 98735558").executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();

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
                          "assignees": [],
                          "languages": [],
                          "githubAppInstallationStatus": "MISSING_PERMISSIONS",
                          "githubAppInstallationPermissionsUpdateUrl": "https://github.com/settings/installations/44741576"
                        }
                        """);
    }
}
