package onlydust.com.marketplace.api.it.api;

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

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
public class ApplicationsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ApplicationRepository applicationRepository;

    private final static UUID projectAppliedTo1 = UUID.fromString("3c22af5d-2cf8-48a1-afa0-c3441df7fb3b");
    private final static UUID projectAppliedTo2 = UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c");

    private static List<ApplicationEntity> applications;

    private final static AtomicBoolean setupDone = new AtomicBoolean();

    @BeforeEach
    synchronized void setupOnce() {
        if (setupDone.compareAndExchange(false, true)) return;

        final var pierre = userAuthHelper.authenticatePierre();
        final var antho = userAuthHelper.authenticateAntho();
        final var olivier = userAuthHelper.authenticateOlivier();
        applications = List.of(
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
        final String jwt = userAuthHelper.authenticateGregoire().jwt();

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
                          "totalItemNumber": 3,
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
                                "githubUserId": 595505,
                                "login": "ofux",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                "isRegistered": true,
                                "globalRank": 4,
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
        final String jwt = userAuthHelper.authenticateGregoire().jwt();

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
        final String jwt = userAuthHelper.authenticateGregoire().jwt();

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
        final UUID applicationId = UUID.randomUUID();
        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                projectAppliedTo1,
                userAuthHelper.authenticatePierre().user().getGithubUserId(),
                Application.Origin.GITHUB,
                1736504583L,
                112L,
                "Highly motivated",
                "Do the math"
        ));

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
        final UUID applicationId = UUID.randomUUID();
        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                projectAppliedTo1,
                olivier.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1736504583L,
                112L,
                "Highly motivated",
                "Do the math"
        ));

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
                            "id": 1736504583,
                            "number": 1112,
                            "title": "Monthly contracting subscription",
                            "status": "OPEN",
                            "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112"
                          },
                          "applicant": {
                            "githubUserId": 595505,
                            "login": "ofux",
                            "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                            "isRegistered": true
                          },
                          "origin": "GITHUB",
                          "motivation": "Highly motivated",
                          "problemSolvingApproach": "Do the math"
                        }
                        """);
    }

    @Test
    void should_ignore_application() {
        // Given
        final var projectLead = userAuthHelper.authenticateGregoire();
        final var application = applications.get(0);

        // When
        client.patch()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(application.id())))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                .bodyValue(new ProjectApplicationPatchRequest()
                        .isIgnored(true))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(applicationRepository.findById(application.id()).orElseThrow().ignoredAt()).isNotNull();


        // When
        client.patch()
                .uri(getApiURI(APPLICATIONS_BY_ID.formatted(application.id())))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                .bodyValue(new ProjectApplicationPatchRequest()
                        .isIgnored(false))
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(applicationRepository.findById(application.id()).orElseThrow().ignoredAt()).isNull();
    }
}
