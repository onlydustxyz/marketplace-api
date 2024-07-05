package onlydust.com.marketplace.api.it.api;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.project.domain.job.ApplicationMailNotifier;
import onlydust.com.marketplace.project.domain.model.Application;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApplicationsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    private ApplicationRepository applicationRepository;

    final UUID projectAppliedTo1 = UUID.fromString("3c22af5d-2cf8-48a1-afa0-c3441df7fb3b");
    final UUID projectAppliedTo2 = UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c");

    @Test
    @Order(0)
    void setupOnce() {
        final var pierre = userAuthHelper.authenticatePierre();
        final var antho = userAuthHelper.authenticateAnthony();
        final var olivier = userAuthHelper.authenticateOlivier();

        applicationRepository.saveAll(List.of(
                fakeApplication(projectAppliedTo1, pierre, 1736474921L, 112L),
                fakeApplication(projectAppliedTo2, pierre, 1736474921L, 113L),
                fakeApplication(projectAppliedTo2, pierre, 1736504583L, 113L),

                fakeApplication(projectAppliedTo1, antho, 1736474921L, 112L),
                fakeApplication(projectAppliedTo2, antho, 1736504583L, 113L),

                fakeApplication(projectAppliedTo1, olivier, 1736474921L, 112L)
        ));
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
    @Order(1)
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
    @Order(1)
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
    @Order(1)
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
    @Order(1)
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
    @Order(1)
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
    @Order(10)
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
    @Order(10)
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
    @Order(10)
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

    @Autowired
    ApplicationMailNotifier applicationMailNotifier;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    OutboxConsumerJob projectMailOutboxJob;

    @Test
    @Order(100)
    void should_send_daily_reports_to_leads_by_email() {
        // Given
        final var gregoire = userAuthHelper.authenticateGregoire();

        // When
        applicationMailNotifier.notifyProjectApplicationsToReview();
        projectMailOutboxJob.run();

        // Then
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getProjectApplicationsToReviewByUserEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(gregoire.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data", equalToJson("""
                                {
                                  "username": "gregcha",
                                  "projects": [
                                    {
                                      "slug": "taco-tuesday",
                                      "name": "Taco Tuesday",
                                      "issues": [
                                        {
                                          "id": 1736474921,
                                          "title": "Documentation by AnthonyBuisset",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 3
                                        },{
                                          "id": 1736504583,
                                          "title": "Monthly contracting subscription",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 2
                                        }
                                      ]
                                    },{
                                      "slug": "starklings",
                                      "name": "Starklings",
                                      "issues": [
                                        {
                                          "id": 1736474921,
                                          "title": "Documentation by AnthonyBuisset",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 1
                                        },{
                                          "id": 1736504583,
                                          "title": "Monthly contracting subscription",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 2
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """, true, false)))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(gregoire.user().getGithubEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustMarketingEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Applications to review daily report")))
        );

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getProjectApplicationsToReviewByUserEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo("46fec596-7a91-422e-8532-5f479e790217")))
                        .withRequestBody(matchingJsonPath("$.message_data", equalToJson("""
                                {
                                  "username": "Blumebee",
                                  "projects": [
                                    {
                                      "slug": "taco-tuesday",
                                      "name": "Taco Tuesday",
                                      "issues": [
                                        {
                                          "id": 1736474921,
                                          "title": "Documentation by AnthonyBuisset",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 3
                                        },{
                                          "id": 1736504583,
                                          "title": "Monthly contracting subscription",
                                          "repoName": "cool.repo.B",
                                          "applicantCount": 2
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """, true, false)))
                        .withRequestBody(matchingJsonPath("$.to", containing("emilie.blum")))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustMarketingEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Applications to review daily report")))
        );
    }
}
