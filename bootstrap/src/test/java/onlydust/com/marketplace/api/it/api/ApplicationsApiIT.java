package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
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
                        "issueId", "1736474921"
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
                        "issueId", "1736504583"
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
                        "issueId", "1736474921"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
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
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true
                              },
                              "recommandationScore": 43
                            },
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
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "recommandationScore": 41
                            },
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
                              "recommandationScore": 33
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(APPLICATIONS, Map.of(
                        "projectId", projectAppliedTo2.toString(),
                        "issueId", "1736504583"
                )))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "projectId": "6239cb20-eece-466a-80a0-742c1071dd3c",
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112"
                              },
                              "applicant": {
                                "githubUserId": 43467246,
                                "login": "AnthonyBuisset",
                                "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                "isRegistered": true
                              },
                              "recommandationScore": 35
                            },
                            {
                              "projectId": "6239cb20-eece-466a-80a0-742c1071dd3c",
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112"
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "recommandationScore": 35
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
                        "applicantId", pierre.user().getGithubUserId().toString()
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "applications": [
                            {
                              "projectId": "6239cb20-eece-466a-80a0-742c1071dd3c",
                              "issue": {
                                "id": 1736474921,
                                "number": 1111,
                                "title": "Documentation by AnthonyBuisset",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111"
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "recommandationScore": 35
                            },
                            {
                              "projectId": "6239cb20-eece-466a-80a0-742c1071dd3c",
                              "issue": {
                                "id": 1736504583,
                                "number": 1112,
                                "title": "Monthly contracting subscription",
                                "status": "OPEN",
                                "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1112"
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "recommandationScore": 35
                            },
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
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "recommandationScore": 41
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
                          "recommandationScore": 36,
                          "availabilityScore": 71,
                          "languageScore": 0,
                          "fidelityScore": 43,
                          "appliedDistinctProjectCount": 3,
                          "pendingApplicationCountOnOtherProjects": 2,
                          "pendingApplicationCountOnThisProject": 2,
                          "motivation": "Highly motivated",
                          "problemSolvingApproach": "Do the math"
                        }
                        """);
    }

}
