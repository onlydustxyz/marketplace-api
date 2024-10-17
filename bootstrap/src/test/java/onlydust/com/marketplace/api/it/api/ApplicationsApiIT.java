package onlydust.com.marketplace.api.it.api;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.ContributorOverviewResponse;
import onlydust.com.marketplace.api.contract.model.IssueApplicantsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.IssueApplicantsPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationPatchRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ApplicationsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ApplicationRepository applicationRepository;

    private final static UUID projectAppliedTo1 = UUID.fromString("3c22af5d-2cf8-48a1-afa0-c3441df7fb3b");
    private final static UUID projectAppliedTo2 = UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c");

    UserAuthHelper.AuthenticatedUser pierre;
    UserAuthHelper.AuthenticatedUser antho;
    UserAuthHelper.AuthenticatedUser olivier;
    UserAuthHelper.AuthenticatedUser gregoire;

    private static List<ApplicationEntity> applications;

    private final static AtomicBoolean setupDone = new AtomicBoolean();

    @BeforeEach
    synchronized void setupOnce() {
        pierre = userAuthHelper.authenticatePierre();
        antho = userAuthHelper.authenticateAntho();
        olivier = userAuthHelper.authenticateOlivier();
        gregoire = userAuthHelper.authenticateGregoire();

        if (setupDone.compareAndExchange(false, true)) return;

        applications = List.of(
                fakeApplication(projectAppliedTo1, gregoire, 1736474921L, 110L),

                fakeApplication(projectAppliedTo1, pierre, 1736474921L, 112L),
                fakeApplication(projectAppliedTo2, pierre, 1736474921L, 113L),
                fakeApplication(projectAppliedTo2, pierre, 1736504583L, 113L),

                fakeApplication(projectAppliedTo1, antho, 1736474921L, 112L),
                fakeApplication(projectAppliedTo2, antho, 1736504583L, 113L),

                fakeApplication(projectAppliedTo1, olivier, 1736474921L, 112L)
        );

        applicationRepository.saveAll(applications);
    }

    public static ApplicationEntity fakeApplication(UUID projectId,
                                                    UserAuthHelper.AuthenticatedUser user,
                                                    long issueId,
                                                    long commentId) {
        return new ApplicationEntity(
                UUID.randomUUID(),
                ZonedDateTime.now(),
                projectId,
                user.user().getGithubUserId(),
                Application.Origin.MARKETPLACE,
                issueId,
                commentId,
                "%s motivations on %s/%s".formatted(user.user().getGithubLogin(), issueId, projectId),
                null
        );
    }

    @Test
    void should_return_forbidden_status_when_caller_is_not_lead() {
        // Given
        final String jwt = userAuthHelper.authenticateHayden().jwt();

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo1.toString(),
                        "issueId", "1736474921",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_return_applications_for_project_and_issue() {
        // Given
        final String jwt = gregoire.jwt();

        // When (no application for this issue on project 1)
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo1.toString(),
                        "issueId", "1736504583",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"applications":[]}
                        """);

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo1.toString(),
                        "issueId", "1736474921",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.applications[?(!@.id)]").isEmpty()
                .jsonPath("$.applications[?(@.id)]").isNotEmpty()
                .jsonPath("$.applications[?(!@.receivedAt)]").isEmpty()
                .jsonPath("$.applications[?(@.receivedAt)]").isNotEmpty()
                .jsonPath("$.applications[0].applicant.globalRank").isEqualTo(1)
                .jsonPath("$.applications[1].applicant.globalRank").isEqualTo(4)
                .jsonPath("$.applications[2].applicant.globalRank").isEqualTo(5)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "project": {
                                "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                "slug": "taco-tuesday",
                                "name": "Taco Tuesday",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                              },
                              "issue": {
                                "id": 1736474921,
                                "number": 1111,
                                "title": "Documentation by AnthonyBuisset",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111",
                                "repo": {
                                  "id": 602953640,
                                  "owner": "od-mocks",
                                  "name": "cool.repo.B",
                                  "description": null,
                                  "htmlUrl": "https://github.com/od-mocks/cool.repo.B"
                                },
                                "author": {
                                  "githubUserId": 112474158,
                                  "login": "onlydust-contributor",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                                  "isRegistered": true,
                                  "id": "cde93e0e-99cf-4722-8aaa-2c27b91e270d"
                                }
                              },
                              "applicant": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "globalRank": 1,
                                "globalRankPercentile": null,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                "slug": "taco-tuesday",
                                "name": "Taco Tuesday",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                              },
                              "issue": {
                                "id": 1736474921,
                                "number": 1111,
                                "title": "Documentation by AnthonyBuisset",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111",
                                "repo": {
                                  "id": 602953640,
                                  "owner": "od-mocks",
                                  "name": "cool.repo.B",
                                  "description": null,
                                  "htmlUrl": "https://github.com/od-mocks/cool.repo.B"
                                },
                                "author": {
                                  "githubUserId": 112474158,
                                  "login": "onlydust-contributor",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                                  "isRegistered": true,
                                  "id": "cde93e0e-99cf-4722-8aaa-2c27b91e270d"
                                }
                              },
                              "applicant": {
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                "isRegistered": true,
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                                "globalRank": 4,
                                "globalRankPercentile": null,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                "slug": "taco-tuesday",
                                "name": "Taco Tuesday",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                              },
                              "issue": {
                                "id": 1736474921,
                                "number": 1111,
                                "title": "Documentation by AnthonyBuisset",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111",
                                "repo": {
                                  "id": 602953640,
                                  "owner": "od-mocks",
                                  "name": "cool.repo.B",
                                  "description": null,
                                  "htmlUrl": "https://github.com/od-mocks/cool.repo.B"
                                },
                                "author": {
                                  "githubUserId": 112474158,
                                  "login": "onlydust-contributor",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                                  "isRegistered": true,
                                  "id": "cde93e0e-99cf-4722-8aaa-2c27b91e270d"
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0",
                                "globalRank": 5,
                                "globalRankPercentile": null,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                "slug": "taco-tuesday",
                                "name": "Taco Tuesday",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                              },
                              "issue": {
                                "id": 1736474921,
                                "number": 1111,
                                "title": "Documentation by AnthonyBuisset",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111",
                                "repo": {
                                  "id": 602953640,
                                  "owner": "od-mocks",
                                  "name": "cool.repo.B",
                                  "description": null,
                                  "htmlUrl": "https://github.com/od-mocks/cool.repo.B"
                                },
                                "author": {
                                  "githubUserId": 112474158,
                                  "login": "onlydust-contributor",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                                  "isRegistered": true,
                                  "id": "cde93e0e-99cf-4722-8aaa-2c27b91e270d"
                                }
                              },
                              "applicant": {
                                "githubUserId": 8642470,
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                "isRegistered": true,
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "globalRank": 6,
                                "globalRankPercentile": null,
                                "globalRankCategory": "A"
                              }
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo2.toString(),
                        "issueId", "1736504583",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.applications[?(!@.id)]").isEmpty()
                .jsonPath("$.applications[?(@.id)]").isNotEmpty()
                .jsonPath("$.applications[?(!@.receivedAt)]").isEmpty()
                .jsonPath("$.applications[?(@.receivedAt)]").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112",
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "globalRank": 1,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112",
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "globalRank": 5,
                                "globalRankCategory": "A"
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_applications_for_project_and_issue_and_is_project_member() {
        // Given
        final String jwt = gregoire.jwt();

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo2.toString(),
                        "issueId", "1736504583",
                        "isApplicantProjectMember", "true",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {"totalPageNumber":0,"totalItemNumber":0,"hasMore":false,"nextPageIndex":0,"applications":[]}
                        """);

    }

    @Test
    void should_return_applications_for_project_and_issue_and_is_not_project_member() {
        // Given
        final String jwt = gregoire.jwt();

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo2.toString(),
                        "issueId", "1736504583",
                        "isApplicantProjectMember", "false",
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.applications[?(!@.id)]").isEmpty()
                .jsonPath("$.applications[?(@.id)]").isNotEmpty()
                .jsonPath("$.applications[?(!@.receivedAt)]").isEmpty()
                .jsonPath("$.applications[?(@.receivedAt)]").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112",
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "globalRank": 1,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112",
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "globalRank": 5,
                                "globalRankCategory": "A"
                              }
                            }
                          ]
                        }
                        """);

    }

    @Test
    void should_return_applications_for_applicant() {
        // Given
        final var pierre = userAuthHelper.authenticatePierre();

        // When (no application for this issue on project 1)
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "applicantId", pierre.user().getGithubUserId().toString(),
                        "pageIndex", "0",
                        "pageSize", "10"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.applications[?(!@.id)]").isEmpty()
                .jsonPath("$.applications[?(@.id)]").isNotEmpty()
                .jsonPath("$.applications[?(!@.receivedAt)]").isEmpty()
                .jsonPath("$.applications[?(@.receivedAt)]").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112",
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "globalRank": 5,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "issue": {
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "globalRank": 5,
                                "globalRankCategory": "A"
                              }
                            },
                            {
                              "project": {
                                "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                "slug": "taco-tuesday",
                                "name": "Taco Tuesday",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg"
                              },
                              "issue": {
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
                                }
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "globalRank": 5,
                                "globalRankCategory": "A"
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_not_found_when_application_by_id_does_not_exist() {
        // Given
        final var pierre = userAuthHelper.authenticatePierre();

        // When (no application for this issue on project 1)
        client.get()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(UUID.randomUUID().toString())))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_return_forbidden_when_caller_is_not_applicant_not_leader() {
        // Given
        final var camille = userAuthHelper.authenticateHayden();
        final var applicationId = applications.get(0).id();

        // When (no application for this issue on project 1)
        client.get()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(applicationId.toString())))
                .header("Authorization", BEARER_PREFIX + camille.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_return_application_by_id() {
        // Given
        final var olivier = userAuthHelper.authenticateOlivier();
        final UUID applicationId = applications.get(6).id();

        // When (no application for this issue on project 1)
        client.get()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projectId": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                          "issue": {
                            "id": 1736474921,
                            "number": 1111,
                            "title": "Documentation by AnthonyBuisset",
                            "status": "OPEN",
                            "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111"
                          },
                          "applicant": {
                            "githubUserId": 595505,
                            "login": "ofux",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                            "isRegistered": true
                          },
                          "origin": "MARKETPLACE",
                          "motivation": "ofux motivations on 1736474921/3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                          "problemSolvingApproach": null
                        }
                        """);
    }

    @Test
    void should_ignore_application() {
        // Given
        final var application = applications.get(0);

        // When
        ignore(application, gregoire, true);
        assertThat(applicationRepository.findById(application.id()).orElseThrow().ignoredAt()).isNotNull();


        // When
        ignore(application, gregoire, false);
        assertThat(applicationRepository.findById(application.id()).orElseThrow().ignoredAt()).isNull();
    }

    @Test
    void should_list_applications() {
        client.get()
                .uri(getApiURI(ISSUES_BY_ID_APPLICANTS.formatted(1736474921L)))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.applicants[?(!@.applicationId)]").isEmpty()
                .jsonPath("$.applicants[?(!@.appliedAt)]").isEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 4,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applicants": [
                            {
                              "contributor": {
                                "bio": "FullStack engineerr",
                                "contacts": [
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/abuisset",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/abuisset",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "DISCORD",
                                    "contact": "antho",
                                    "visibility": null
                                  }
                                ],
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true,
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                                "globalRank": 1,
                                "globalRankPercentile": 0.000041734485205125,
                                "globalRankCategory": "A"
                              },
                              "projects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                },
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                  "slug": "pizzeria-yoshi-",
                                  "name": "Pizzeria Yoshi !",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                                },
                                {
                                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                  "slug": "qa-new-contributions",
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                },
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                },
                                {
                                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                                  "slug": "zero-title-4",
                                  "name": "Zero title 4",
                                  "logoUrl": null
                                },
                                {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                }
                              ],
                              "categories": null,
                              "languages": [
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "slug": "cairo",
                                  "name": "Cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
                                {
                                  "id": "6b3f8a21-8ae9-4f73-81df-06aeaddbaf42",
                                  "slug": "java",
                                  "name": "Java",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-java.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-java.png"
                                },
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "ecosystems": [
                                {
                                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                  "name": "Aptos",
                                  "url": "https://aptosfoundation.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                  "bannerUrl": null,
                                  "slug": "aptos",
                                  "hidden": null
                                },
                                {
                                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                                  "name": "Avail",
                                  "url": "https://www.availproject.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                                  "bannerUrl": null,
                                  "slug": "avail",
                                  "hidden": null
                                },
                                {
                                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                  "name": "Aztec",
                                  "url": "https://aztec.network/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                  "bannerUrl": null,
                                  "slug": "aztec",
                                  "hidden": null
                                },
                                {
                                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                  "name": "Ethereum",
                                  "url": "https://ethereum.foundation/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                  "bannerUrl": null,
                                  "slug": "ethereum",
                                  "hidden": null
                                },
                                {
                                  "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                                  "name": "Lava",
                                  "url": "https://www.lavanet.xyz/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
                                  "bannerUrl": null,
                                  "slug": "lava",
                                  "hidden": null
                                },
                                {
                                  "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                                  "name": "Optimism",
                                  "url": "https://www.optimism.io/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png",
                                  "bannerUrl": null,
                                  "slug": "optimism",
                                  "hidden": null
                                },
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet",
                                  "hidden": null
                                },
                                {
                                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                  "name": "Zama",
                                  "url": "https://www.zama.ai/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                  "bannerUrl": null,
                                  "slug": "zama",
                                  "hidden": null
                                }
                              ],
                              "projectContributorLabels": null,
                              "countryCode": "FR",
                              "totalRewardedUsdAmount": {
                                "value": 2692632.50,
                                "trend": "UP"
                              },
                              "rewardCount": {
                                "value": 21,
                                "trend": "UP"
                              },
                              "issueCount": {
                                "value": 46,
                                "trend": "UP"
                              },
                              "prCount": {
                                "value": 817,
                                "trend": "UP"
                              },
                              "codeReviewCount": {
                                "value": 582,
                                "trend": "UP"
                              },
                              "contributionCount": {
                                "value": 1445,
                                "trend": "UP"
                              }
                            },
                            {
                              "contributor": {
                                "bio": "Je me lève très tôt et mange à midi pile, n'en déplaise aux grincheux",
                                "contacts": null,
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true,
                                "id": "fc92397c-3431-4a84-8054-845376b630a0",
                                "globalRank": 5,
                                "globalRankPercentile": 0.00020867242602562499,
                                "globalRankCategory": "A"
                              },
                              "projects": [
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                  "slug": "qa-new-contributions",
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                },
                                {
                                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                                  "slug": "zero-title-4",
                                  "name": "Zero title 4",
                                  "logoUrl": null
                                },
                                {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                {
                                  "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                                  "slug": "toto",
                                  "name": "toto",
                                  "logoUrl": null
                                }
                              ],
                              "categories": null,
                              "languages": [
                                {
                                  "id": "6b3f8a21-8ae9-4f73-81df-06aeaddbaf42",
                                  "slug": "java",
                                  "name": "Java",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-java.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-java.png"
                                },
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "ecosystems": [
                                {
                                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                                  "name": "Avail",
                                  "url": "https://www.availproject.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                                  "bannerUrl": null,
                                  "slug": "avail",
                                  "hidden": null
                                },
                                {
                                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                  "name": "Aztec",
                                  "url": "https://aztec.network/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                  "bannerUrl": null,
                                  "slug": "aztec",
                                  "hidden": null
                                },
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet",
                                  "hidden": null
                                }
                              ],
                              "projectContributorLabels": null,
                              "countryCode": null,
                              "totalRewardedUsdAmount": {
                                "value": 6060.00,
                                "trend": "UP"
                              },
                              "rewardCount": {
                                "value": 6,
                                "trend": "UP"
                              },
                              "issueCount": {
                                "value": 0,
                                "trend": "STABLE"
                              },
                              "prCount": {
                                "value": 263,
                                "trend": "UP"
                              },
                              "codeReviewCount": {
                                "value": 417,
                                "trend": "UP"
                              },
                              "contributionCount": {
                                "value": 680,
                                "trend": "UP"
                              }
                            },
                            {
                              "contributor": {
                                "bio": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                                "contacts": [
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/gregoirecharles",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "WHATSAPP",
                                    "contact": "+33683744020",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/gregcha",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "LINKEDIN",
                                    "contact": "https://www.linkedin.com/in/gregoirecharles",
                                    "visibility": null
                                  }
                                ],
                                "githubUserId": 8642470,
                                "login": "gregcha",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                "isRegistered": true,
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964",
                                "globalRank": 6,
                                "globalRankPercentile": 0.00025040691123075,
                                "globalRankCategory": "A"
                              },
                              "projects": [
                                {
                                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "slug": "bretzel",
                                  "name": "Bretzel",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                                }
                              ],
                              "categories": null,
                              "languages": null,
                              "ecosystems": [
                                {
                                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                  "name": "Aptos",
                                  "url": "https://aptosfoundation.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                  "bannerUrl": null,
                                  "slug": "aptos",
                                  "hidden": null
                                },
                                {
                                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                  "name": "Ethereum",
                                  "url": "https://ethereum.foundation/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                  "bannerUrl": null,
                                  "slug": "ethereum",
                                  "hidden": null
                                },
                                {
                                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                  "name": "Zama",
                                  "url": "https://www.zama.ai/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                  "bannerUrl": null,
                                  "slug": "zama",
                                  "hidden": null
                                }
                              ],
                              "projectContributorLabels": null,
                              "countryCode": null,
                              "totalRewardedUsdAmount": {
                                "value": 85057.50,
                                "trend": "UP"
                              },
                              "rewardCount": {
                                "value": 64,
                                "trend": "UP"
                              },
                              "issueCount": {
                                "value": 2,
                                "trend": "UP"
                              },
                              "prCount": {
                                "value": 3,
                                "trend": "UP"
                              },
                              "codeReviewCount": {
                                "value": 0,
                                "trend": "STABLE"
                              },
                              "contributionCount": {
                                "value": 5,
                                "trend": "UP"
                              }
                            },
                            {
                              "contributor": {
                                "bio": "totot",
                                "contacts": [
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/fuxeto",
                                    "visibility": null
                                  },
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/ofux",
                                    "visibility": null
                                  }
                                ],
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                "isRegistered": true,
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                                "globalRank": 4,
                                "globalRankPercentile": 0.0001669379408205,
                                "globalRankCategory": "A"
                              },
                              "projects": [
                                {
                                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "slug": "b-conseil",
                                  "name": "B Conseil",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png"
                                },
                                {
                                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "slug": "mooooooonlight",
                                  "name": "Mooooooonlight",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg"
                                },
                                {
                                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                                  "slug": "no-sponsors",
                                  "name": "No sponsors",
                                  "logoUrl": null
                                },
                                {
                                  "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                  "slug": "pizzeria-yoshi-",
                                  "name": "Pizzeria Yoshi !",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png"
                                },
                                {
                                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                                  "slug": "qa-new-contributions",
                                  "name": "QA new contributions",
                                  "logoUrl": null
                                },
                                {
                                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "slug": "zama",
                                  "name": "Zama",
                                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M"
                                },
                                {
                                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                                  "slug": "zero-title-4",
                                  "name": "Zero title 4",
                                  "logoUrl": null
                                },
                                {
                                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "slug": "kaaper",
                                  "name": "kaaper",
                                  "logoUrl": null
                                },
                                {
                                  "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                                  "slug": "toto",
                                  "name": "toto",
                                  "logoUrl": null
                                }
                              ],
                              "categories": null,
                              "languages": [
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "slug": "cairo",
                                  "name": "Cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
                                {
                                  "id": "6b3f8a21-8ae9-4f73-81df-06aeaddbaf42",
                                  "slug": "java",
                                  "name": "Java",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-java.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-java.png"
                                },
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "ecosystems": [
                                {
                                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                                  "name": "Avail",
                                  "url": "https://www.availproject.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                                  "bannerUrl": null,
                                  "slug": "avail",
                                  "hidden": null
                                },
                                {
                                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                  "name": "Aztec",
                                  "url": "https://aztec.network/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                  "bannerUrl": null,
                                  "slug": "aztec",
                                  "hidden": null
                                },
                                {
                                  "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                                  "name": "Lava",
                                  "url": "https://www.lavanet.xyz/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
                                  "bannerUrl": null,
                                  "slug": "lava",
                                  "hidden": null
                                },
                                {
                                  "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                                  "name": "Optimism",
                                  "url": "https://www.optimism.io/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png",
                                  "bannerUrl": null,
                                  "slug": "optimism",
                                  "hidden": null
                                },
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet",
                                  "hidden": null
                                }
                              ],
                              "projectContributorLabels": null,
                              "countryCode": null,
                              "totalRewardedUsdAmount": {
                                "value": 20855.02,
                                "trend": "UP"
                              },
                              "rewardCount": {
                                "value": 15,
                                "trend": "UP"
                              },
                              "issueCount": {
                                "value": 14,
                                "trend": "UP"
                              },
                              "prCount": {
                                "value": 514,
                                "trend": "UP"
                              },
                              "codeReviewCount": {
                                "value": 469,
                                "trend": "UP"
                              },
                              "contributionCount": {
                                "value": 997,
                                "trend": "UP"
                              }
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_list_ignored_applications() {
        // Given
        final var ignoredApplication = applications.get(0);
        ignore(ignoredApplication, gregoire, true);

        // When
        assertApplications(ignoredApplication.issueId(), Map.of("isIgnored", "true"), response -> {
            // Then
            assertThat(response.getApplicants())
                    .isNotEmpty()
                    .extracting(IssueApplicantsPageItemResponse::getApplicationId)
                    .allMatch(id -> id.equals(ignoredApplication.id()));
        });

        // When
        assertApplications(ignoredApplication.issueId(), Map.of("isIgnored", "false"), response -> {
            // Then
            assertThat(response.getApplicants())
                    .isNotEmpty()
                    .extracting(IssueApplicantsPageItemResponse::getApplicationId)
                    .allMatch(id -> !id.equals(ignoredApplication.id()));
        });
    }

    @Test
    void should_list_project_members() {
        // Given
        final var issueId = 1736474921L;

        // When
        assertApplications(issueId, Map.of("isApplicantProjectMember", "true"), response -> {
            // Then
            assertThat(response.getApplicants())
                    .isNotEmpty()
                    .extracting(IssueApplicantsPageItemResponse::getContributor)
                    .extracting(ContributorOverviewResponse::getGithubUserId)
                    .allMatch(id -> id.equals(gregoire.githubUserId().value()));
        });

        // When
        assertApplications(issueId, Map.of("isApplicantProjectMember", "false"), response -> {
            // Then
            assertThat(response.getApplicants())
                    .isNotEmpty()
                    .extracting(IssueApplicantsPageItemResponse::getContributor)
                    .extracting(ContributorOverviewResponse::getGithubUserId)
                    .allMatch(id -> !id.equals(gregoire.githubUserId().value()));
        });
    }

    private void ignore(ApplicationEntity application, UserAuthHelper.AuthenticatedUser projectLead, boolean isIgnored) {
        client.patch()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(application.id())))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                .bodyValue(new ProjectApplicationPatchRequest()
                        .isIgnored(isIgnored))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    private void assertApplications(final @NonNull Long issueId,
                                    final @NonNull Map<String, String> params,
                                    final @NonNull Consumer<IssueApplicantsPageResponse> asserter) {
        client.get()
                .uri(getApiURI(ISSUES_BY_ID_APPLICANTS.formatted(issueId), params))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(IssueApplicantsPageResponse.class)
                .consumeWith(System.out::println)
                .consumeWith(r -> asserter.accept(r.getResponseBody()));
    }
}
