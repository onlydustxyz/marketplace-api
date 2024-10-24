package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.GithubIssuePageItemResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssuePageResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import org.assertj.core.api.AbstractListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.it.api.ApplicationsApiIT.fakeApplication;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ProjectsGetIssuesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PostgresBiProjectorAdapter biProjectorAdapter;

    private final static UUID projectAppliedTo1 = UUID.fromString("27ca7e18-9e71-468f-8825-c64fe6b79d66");
    private final static UUID projectAppliedTo2 = UUID.fromString("57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8");
    private final static AtomicBoolean setupDone = new AtomicBoolean();

    private static UserAuthHelper.AuthenticatedUser projectLead;

    @BeforeEach
    void setup() {
        projectLead = userAuthHelper.authenticateUser(134486697L);

        if (setupDone.compareAndExchange(false, true)) return;

        final var pierre = userAuthHelper.authenticatePierre();
        final var antho = userAuthHelper.authenticateAntho();
        final var olivier = userAuthHelper.authenticateOlivier();

        final var applications = List.of(
                // 1643865031L has 2 applicants on project 2
                // 1652216317L has 2 applicants on project 1 and 1 applicant on project 2
                fakeApplication(projectAppliedTo1, pierre, 1643866301L, 112L),
                fakeApplication(projectAppliedTo2, pierre, 1643865031L, 113L),

                fakeApplication(projectAppliedTo2, antho, 1643865031L, 112L),
                fakeApplication(projectAppliedTo2, antho, 1643866301L, 113L),

                fakeApplication(projectAppliedTo1, olivier, 1643866301L, 112L)
        );

        databaseHelper.executeInTransaction(() -> {
            applicationRepository.saveAll(applications);
            applications.forEach(a -> biProjectorAdapter.onApplicationCreated(new Application(
                    Application.Id.of(a.id()),
                    ProjectId.of(a.projectId()),
                    a.applicantId(),
                    a.origin(),
                    a.receivedAt(),
                    GithubIssue.Id.of(a.issueId()),
                    GithubComment.Id.of(a.commentId()),
                    a.commentBody()
            )));
        });
    }

    @Test
    void should_return_project_issues() {
        client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), Map.of("pageIndex", "0", "pageSize", "3")))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 19,
                          "totalItemNumber": 56,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
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
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                              },
                              "createdAt": "2023-03-28T12:39:14Z",
                              "closedAt": null,
                              "body": "Real cool documentation",
                              "labels": [],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "isRegistered": true,
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0"
                                }
                              ],
                              "assignees": []
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
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                              },
                              "createdAt": "2023-03-28T12:40:02Z",
                              "closedAt": null,
                              "body": "Real cool documentation",
                              "labels": [],
                              "applicants": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true,
                                  "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "isRegistered": true,
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                  "isRegistered": true,
                                  "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                                }
                              ],
                              "assignees": []
                            },
                            {
                              "id": 1643867196,
                              "number": 14,
                              "title": "Documentation by AnthonyBuisset",
                              "status": "COMPLETED",
                              "htmlUrl": "https://github.com/od-mocks/cool-repo-A/issues/14",
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
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                              },
                              "createdAt": "2023-03-28T12:40:35Z",
                              "closedAt": "2023-08-30T09:20:48Z",
                              "body": "Real cool documentation",
                              "labels": [],
                              "applicants": [],
                              "assignees": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_assigned_project_issues() {
        assertIssues(Map.of("isAssigned", "true"))
                .allMatch(issue -> !issue.getAssignees().isEmpty());
    }

    @Test
    void should_return_unassigned_project_issues() {
        assertIssues(Map.of("isAssigned", "false"))
                .allMatch(issue -> issue.getAssignees().isEmpty());
    }

    @Test
    void should_return_unassigned_open_project_issues() {
        assertIssues(Map.of("isAssigned", "false", "statuses", "OPEN"))
                .allMatch(issue -> issue.getAssignees().isEmpty() && issue.getStatus() == GithubIssueStatus.OPEN);
    }

    @Test
    void should_return_applied_project_issues() {
        assertIssues(Map.of("isApplied", "true"))
                .allMatch(issue -> !issue.getApplicants().isEmpty());
    }

    @Test
    void should_return_not_applied_project_issues() {
        assertIssues(Map.of("isApplied", "false"))
                .allMatch(issue -> issue.getApplicants().isEmpty());
    }

    private AbstractListAssert<?, ? extends List<? extends GithubIssuePageItemResponse>, GithubIssuePageItemResponse> assertIssues(Map<String, String> params) {
        final var q = new HashMap<String, String>();
        q.put("pageIndex", "0");
        q.put("pageSize", "30");
        q.putAll(params);

        final var issues = client.get()
                .uri(getApiURI(PROJECT_PUBLIC_ISSUES.formatted(projectAppliedTo1), q))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(GithubIssuePageResponse.class)
                .returnResult()
                .getResponseBody()
                .getIssues();

        return assertThat(issues).isNotEmpty();
    }
}
