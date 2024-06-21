package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ProjectIssuesPageResponse;
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
        final var antho = userAuthHelper.authenticateAnthony();
        final var olivier = userAuthHelper.authenticateOlivier();

        applicationRepository.saveAll(List.of(
                fakeApplication(projectAppliedTo1, pierre, 1651834617L, 112L),
                fakeApplication(projectAppliedTo2, pierre, 1652216316L, 113L),

                fakeApplication(projectAppliedTo1, antho, 1652216316L, 112L),
                fakeApplication(projectAppliedTo2, antho, 1651834617L, 113L),

                fakeApplication(projectAppliedTo1, olivier, 1651834617L, 112L)
        ));
    }

    @Test
    @Order(1)
    void should_return_forbidden_status_when_caller_is_not_lead() {
        // Given
        final String jwt = userAuthHelper.authenticateHayden().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(1)
    void should_return_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        client.get()
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 564,
                          "totalItemNumber": 1691,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1564131775,
                              "number": 1,
                              "title": "testing",
                              "status": "OPEN",
                              "createdAt": "2023-01-31T11:26:54Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/gregcha/crew-app/issues/1",
                              "author": {
                                "githubUserId": 8642470,
                                "login": "gregcha",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "isRegistered": true
                              },
                              "repository": {
                                "id": 302082426,
                                "owner": "gregcha",
                                "name": "crew-app",
                                "description": null,
                                "htmlUrl": "https://github.com/gregcha/crew-app"
                              },
                              "applicants": [],
                              "assignees": []
                            },
                            {
                              "id": 1642022365,
                              "number": 6,
                              "title": "This is a new issue",
                              "status": "COMPLETED",
                              "createdAt": "2023-03-27T12:56:04Z",
                              "closedAt": "2023-08-30T09:20:48Z",
                              "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/6",
                              "author": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "isRegistered": true
                              },
                              "repository": {
                                "id": 602953043,
                                "owner": "od-mocks",
                                "name": "cool-repo-A",
                                "description": "This is repo A for our e2e tests",
                                "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                              },
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ]
                            },
                            {
                              "id": 1642022454,
                              "number": 7,
                              "title": "This one has been cancelled",
                              "status": "CANCELLED",
                              "createdAt": "2023-03-27T12:56:32Z",
                              "closedAt": "2023-03-27T12:57:25Z",
                              "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/7",
                              "author": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "isRegistered": true
                              },
                              "repository": {
                                "id": 602953043,
                                "owner": "od-mocks",
                                "name": "cool-repo-A",
                                "description": "This is repo A for our e2e tests",
                                "htmlUrl": "https://github.com/od-mocks/cool-repo-A"
                              },
                              "applicants": [],
                              "assignees": []
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
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "true")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectIssuesPageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getAssignees()).isNotEmpty());
    }

    @Test
    @Order(1)
    void should_return_unassigned_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isAssigned", "false")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectIssuesPageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getAssignees()).isEmpty());
    }

    @Test
    @Order(1)
    void should_return_applied_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "true")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectIssuesPageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getApplicants()).isNotEmpty());
    }

    @Test
    @Order(1)
    void should_return_not_applied_project_issues() {
        // Given
        final String jwt = userAuthHelper.authenticateUser(134486697L).jwt();

        final var issues = client.get()
                .uri(getApiURI(PROJECTS_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "30", "isApplied", "false")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ProjectIssuesPageResponse.class).returnResult().getResponseBody().getIssues();

        assertThat(issues).isNotEmpty();
        issues.forEach(issue -> assertThat(issue.getApplicants()).isEmpty());
    }

}
