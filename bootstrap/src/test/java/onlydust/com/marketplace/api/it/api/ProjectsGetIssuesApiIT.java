package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.GithubIssuePageResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.it.api.ApplicationsApiIT.fakeApplication;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetIssuesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ApplicationRepository applicationRepository;

    final UUID projectAppliedTo1 = UUID.fromString("27ca7e18-9e71-468f-8825-c64fe6b79d66");
    final UUID projectAppliedTo2 = UUID.fromString("57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8");

    @Test
    @Order(0)
    void setupOnce() {
        final var pierre = userAuthHelper.authenticatePierre();
        final var antho = userAuthHelper.authenticateAntho();
        final var olivier = userAuthHelper.authenticateOlivier();

        applicationRepository.saveAll(List.of(
                // 1652216316L has 2 applicants on project 2
                // 1652216317L has 2 applicants on project 1 and 1 applicant on project 2
                fakeApplication(projectAppliedTo1, pierre, 1651834617L, 112L),
                fakeApplication(projectAppliedTo2, pierre, 1652216316L, 113L),

                fakeApplication(projectAppliedTo2, antho, 1652216316L, 112L),
                fakeApplication(projectAppliedTo2, antho, 1651834617L, 113L),

                fakeApplication(projectAppliedTo1, olivier, 1651834617L, 112L)
        ));
    }

    @Test
    @Order(1)
    void should_return_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 20,
                          "totalItemNumber": 58,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1564131775,
                              "number": 1,
                              "title": "testing",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/gregcha/crew-app/issues/1",
                              "repo": {
                                "id": 302082426,
                                "owner": "gregcha",
                                "name": "crew-app",
                                "description": null,
                                "htmlUrl": "https://github.com/gregcha/crew-app"
                              },
                              "author": {
                                "githubUserId": 8642470,
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "createdAt": "2023-01-31T11:26:54Z",
                              "closedAt": null,
                              "body": "testing issues",
                              "labels": [],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1643865031,
                              "number": 12,
                              "title": "Documentation by AnthonyBuisset",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/12",
                              "repo": {
                                "id": 602953043,
                                "owner": "od-mocks",
                                "name": "cool-repo-A",
                                "description": "This is repo A for our e2e tests",
                                "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                              },
                              "author": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "createdAt": "2023-03-28T12:39:14Z",
                              "closedAt": null,
                              "body": "Real cool documentation",
                              "labels": [],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            },
                            {
                              "id": 1643866301,
                              "number": 13,
                              "title": "Documentation by AnthonyBuisset",
                              "status": "OPEN",
                              "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/13",
                              "repo": {
                                "id": 602953043,
                                "owner": "od-mocks",
                                "name": "cool-repo-A",
                                "description": "This is repo A for our e2e tests",
                                "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                              },
                              "author": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": null
                              },
                              "createdAt": "2023-03-28T12:40:02Z",
                              "closedAt": null,
                              "body": "Real cool documentation",
                              "labels": [],
                              "applicants": [],
                              "assignees": [],
                              "currentUserApplication": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(1)
    void should_return_assigned_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "true")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getAssignees()).isNotEmpty());
    }

    @Test
    @Order(1)
    void should_return_unassigned_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "false")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getAssignees()).isEmpty());
    }

    @Test
    @Order(1)
    void should_return_unassigned_open_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of(
                        "pageIndex", "0",
                        "pageSize", "30",
                        "isAssigned", "false",
                        "statuses", "OPEN")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        assertThat(issues).allMatch(issue -> issue.getAssignees().isEmpty() && issue.getStatus() == GithubIssueStatus.OPEN);
    }

    @Test
    @Order(1)
    void should_return_applied_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "true")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getApplicants()).isNotEmpty());
    }

    @Test
    @Order(1)
    void should_return_not_applied_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "false")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getApplicants()).isEmpty());
    }

}
