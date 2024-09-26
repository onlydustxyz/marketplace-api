package onlydust.com.marketplace.api.it.api;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationCreateRequest;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationCreateResponse;
import onlydust.com.marketplace.api.contract.model.ProjectApplicationUpdateRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndexingEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueDeleted;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@TagMe
public class MeProjectApplicationIT extends AbstractMarketplaceApiIT {
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    IndexingEventRepository indexingEventRepository;
    @Autowired
    OutboxConsumerJob indexingEventsOutboxJob;
    @Autowired
    SlackApiAdapter slackApiAdapter;
    @Autowired
    PosthogProperties posthogProperties;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    HackathonStoragePort hackathonStoragePort;

    @BeforeEach
    void setUp() {
        indexerApiWireMockServer.resetAll();
        Mockito.reset(slackApiAdapter);
    }

    @Test
    void should_apply_to_project() {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final Long issueId = 1974127467L;
        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        final var request = new ProjectApplicationCreateRequest()
                .projectId(projectId)
                .issueId(issueId)
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach);

        githubWireMockServer.stubFor(post(urlEqualTo("/repositories/380954304/issues/7/comments"))
                .withRequestBody(containing("https://local-app.onlydust.com/p/bretzel")
                        .and(containing(motivations))
                        .and(containing(problemSolvingApproach)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 123456789
                                }
                                """)));

        // When
        final var applicationId = client.post()
                .uri(getApiURI(ME_APPLICATIONS))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProjectApplicationCreateResponse.class)
                .returnResult().getResponseBody().getId();

        final var application = applicationRepository.findById(applicationId).orElseThrow();
        assertThat(application.projectId()).isEqualTo(projectId);
        assertThat(application.applicantId()).isEqualTo(user.user().getGithubUserId());
        assertThat(application.issueId()).isEqualTo(issueId);
        assertThat(application.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(application.commentId()).isEqualTo(123456789L);
        assertThat(application.motivations()).isEqualTo(motivations);
        assertThat(application.problemSolvingApproach()).isEqualTo(problemSolvingApproach);

        trackingOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("issue_applied")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(user.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['application_id']", equalTo(applicationId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['project_id']", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['issue_id']", equalTo(issueId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['applicant_github_id']", equalTo(user.user().getGithubUserId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['origin']", equalTo("MARKETPLACE")))
        );
    }

    @Test
    void should_reject_as_forbidden_upon_unauthorized_application() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();
        final Long issueId = 1974127467L;
        final var motivations = faker.lorem().paragraph();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        final var request = new ProjectApplicationCreateRequest()
                .projectId(projectId)
                .issueId(issueId)
                .motivation(motivations);

        githubWireMockServer.stubFor(post(urlEqualTo("/repositories/380954304/issues/7/comments"))
                .willReturn(unauthorized()));

        final var existingApplicationCount = applicationRepository.count();

        // When
        client.post()
                .uri(getApiURI(ME_APPLICATIONS))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();

        assertThat(applicationRepository.count()).isEqualTo(existingApplicationCount);
    }

    @Test
    void should_not_be_able_to_apply_twice() {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final var issueId = 1736474921L;
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        applicationRepository.save(new ApplicationEntity(
                UUID.randomUUID(),
                ZonedDateTime.now(),
                projectId,
                user.user().getGithubUserId(),
                Application.Origin.MARKETPLACE,
                issueId,
                111L,
                "My motivations",
                null
        ));

        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();

        final var request = new ProjectApplicationCreateRequest()
                .projectId(projectId)
                .issueId(issueId)
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach);

        // When
        client.post()
                .uri(getApiURI(ME_APPLICATIONS))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                // Then
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void should_update_application() {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                user.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1974137199L,
                111L,
                "My motivations",
                null
        ));

        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();

        githubWireMockServer.stubFor(patch(urlEqualTo("/repositories/380954304/issues/comments/111"))
                .withRequestBody(containing("https://local-app.onlydust.com/p/bretzel")
                        .and(containing(motivations))
                        .and(containing(problemSolvingApproach)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                {
                                    "id": 111
                                }
                                """)));

        final var request = new ProjectApplicationUpdateRequest()
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach);

        // When
        client.put()
                .uri(getApiURI(ME_APPLICATION.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        final var application = applicationRepository.findById(applicationId).orElseThrow();
        assertThat(application.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(application.motivations()).isEqualTo(motivations);
        assertThat(application.problemSolvingApproach()).isEqualTo(problemSolvingApproach);

        githubWireMockServer.verify(patchRequestedFor(urlEqualTo("/repositories/380954304/issues/comments/111")));
    }

    @Test
    void should_delete_my_github_application() {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                user.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1952203217L,
                111L,
                "My motivations",
                null
        ));

        // When
        client.delete()
                .uri(getApiURI(APPLICATION.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(applicationRepository.findById(applicationId)).isEmpty();
        githubWireMockServer.verify(0, deleteRequestedFor(anyUrl()));
    }

    @Test
    void should_delete_my_marketplace_application() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                user.user().getGithubUserId(),
                Application.Origin.MARKETPLACE,
                1952203217L,
                111L,
                "My motivations",
                null
        ));

        // When
        client.delete()
                .uri(getApiURI(APPLICATION.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(applicationRepository.findById(applicationId)).isEmpty();
        githubWireMockServer.verify(deleteRequestedFor(urlEqualTo("/repositories/380954304/issues/comments/111")));
    }

    @Test
    void should_delete_an_application_as_project_lead() {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final var projectLead = userAuthHelper.authenticateGregoire();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                user.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1974125983L,
                111L,
                "My motivations",
                null
        ));

        // When
        client.delete()
                .uri(getApiURI(APPLICATION.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(applicationRepository.findById(applicationId)).isEmpty();
        githubWireMockServer.verify(0, deleteRequestedFor(anyUrl()));
    }

    @Test
    void should_approve_an_application_as_project_lead() throws InterruptedException {
        // Given
        final var user = userAuthHelper.authenticateAntho();
        final var projectLead = userAuthHelper.authenticateGregoire();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                user.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1974125983L,
                111L,
                "My motivations",
                null
        ));

        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        applicationRepository.save(new ApplicationEntity(
                UUID.randomUUID(),
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                pierre.user().getGithubUserId(),
                Application.Origin.GITHUB,
                1974125983L,
                112L,
                "My motivations 2",
                null
        ));


        githubWireMockServer.stubFor(post(urlEqualTo("/app/installations/44637372/access_tokens"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("""
                                {
                                    "token": "GITHUB_APP_PERSONAL_ACCESS_TOKEN",
                                    "permissions": {
                                        "issues": "write"
                                    }
                                }
                                """)
                ));

        githubWireMockServer.stubFor(post(urlEqualTo("/repositories/380954304/issues/6/assignees"))
                .withHeader("Authorization", matching("Bearer GITHUB_APP_PERSONAL_ACCESS_TOKEN"))
                .withRequestBody(equalToJson("""
                        {
                          "assignees" : [ "AnthonyBuisset" ]
                        }
                        """))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("""
                                {
                                    "id": 1974125983
                                }
                                """)
                ));

        final var expectedComment = """
                The maintainer @gregcha has assigned @AnthonyBuisset to this issue via [OnlyDust](https://local-app.onlydust.com/p/bretzel) Platform.\\n\
                Good luck!\\n\
                """;

        githubWireMockServer.stubFor(post(urlEqualTo("/repositories/380954304/issues/6/comments"))
                .withHeader("Authorization", matching("Bearer GITHUB_APP_PERSONAL_ACCESS_TOKEN"))
                .withRequestBody(equalToJson("""
                        {
                            "body": "%s"
                        }
                        """.formatted(expectedComment)))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("""
                                {
                                    "id": 123456789
                                }
                                """)
                ));

        // When
        client.post()
                .uri(getApiURI(APPLICATION_ACCEPT.formatted(applicationId)))
                .header("Authorization", BEARER_PREFIX + projectLead.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        githubWireMockServer.verify(postRequestedFor(urlEqualTo("/repositories/380954304/issues/6/assignees")));

        Thread.sleep(1000);

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getProjectApplicationAcceptedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(user.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data", equalToJson("""
                                {
                                  "username" : "AnthonyBuisset",
                                  "title": "Issue application accepted",
                                  "description": "We are excited to inform you that your application to the issue <b>Test #7</b> in the <b>Bretzel</b> project has been assigned to you! Thank you for your interest and willingness to contribute to our project.",
                                  "issue": {
                                      "title": "Test #7",
                                      "description": "test",
                                      "repository": "bretzel-app",
                                      "detailsUrl": "https://develop-app.onlydust.com/applications"
                                  }
                                }
                                """, true, false)))
                        .withRequestBody(matchingJsonPath("$.to", containing("abuisset")))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Your application has been accepted!")))
        );

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getProjectApplicationRefusedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(pierre.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data", equalToJson("""
                                {
                                  "username" : "PierreOucif",
                                  "title": "Issue application refused",
                                  "description": "Thank you for your interest in project <b>Bretzel</b>.<br /><br />We wanted to inform you that the issue Test #7 you applied for <b>has been assigned to another candidate</b>. However, we encourage you to continue exploring other projects and applying to different opportunities that match your skills and interests.",
                                  "button" : {
                                                "text": "Explore more projects",
                                                "link": "https://develop-app.onlydust.com/projects"
                                              }
                                }
                                """, true, false)))
                        .withRequestBody(matchingJsonPath("$.to", equalTo("pierre.oucif@gadz.org")))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Issue application refused")))
        );


    }

    @Test
    void should_detect_github_application() {
        // Given
        final var commentId = faker.number().randomNumber(10, true);
        final Long issueId = 1930092330L;
        final var repoId = 466482535L;
        final var antho = userAuthHelper.authenticateAntho();
        final var commentBody = faker.lorem().sentence();

        indexingEventRepository.saveEvent(OnGithubCommentCreated.builder()
                .id(commentId)
                .issueId(issueId)
                .repoId(repoId)
                .authorId(antho.user().getGithubUserId())
                .createdAt(ZonedDateTime.now().minusSeconds(2))
                .body(commentBody)
                .build());
        indexingEventRepository.flush();

        langchainWireMockServer.stubFor(post(urlEqualTo("/chat/completions"))
                .withHeader("Authorization", equalTo("Bearer OPENAI_API_KEY"))
                .withRequestBody(matchingJsonPath("model", equalTo("gpt-4o-2024-08-06")))
                .withRequestBody(matchingJsonPath("messages[?(@.role == 'user')].content", containing(commentBody)))
                .withRequestBody(matchingJsonPath("temperature", equalTo("0.0")))
                .willReturn(okJson("""
                        {
                          "id": "chatcmpl-A4Npk7YEDyndqpn2WLKqNmBNMBBd0",
                          "object": "chat.completion",
                          "created": 1725608292,
                          "model": "gpt-4o-2024-08-06",
                          "choices": [
                            {
                              "index": 0,
                              "message": {
                                "role": "assistant",
                                "content": "true",
                                "refusal": null
                              },
                              "logprobs": null,
                              "finish_reason": "stop"
                            }
                          ],
                          "usage": {
                            "prompt_tokens": 208,
                            "completion_tokens": 1,
                            "total_tokens": 209
                          },
                          "system_fingerprint": "fp_8e1177b306"
                        }
                        """)));

        // When
        indexingEventsOutboxJob.run();

        // Then
        final var applications = applicationRepository.findAllByCommentId(commentId);
        assertThat(applications).hasSize(1);
        final var application = applications.get(0);
        assertThat(application.projectId()).isEqualTo(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"));
        assertThat(application.issueId()).isEqualTo(issueId);
        assertThat(application.applicantId()).isEqualTo(antho.user().getGithubUserId());
        assertThat(application.origin()).isEqualTo(Application.Origin.GITHUB);
        assertThat(application.motivations()).isEqualTo(commentBody);
        assertThat(application.problemSolvingApproach()).isNull();

        indexerApiWireMockServer.verify(putRequestedFor(urlEqualTo("/api/v1/users/" + antho.user().getGithubUserId())));
        verify(slackApiAdapter).onApplicationCreated(any(Application.class));

        trackingOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("issue_applied")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['application_id']", equalTo(application.id().toString())))
                .withRequestBody(matchingJsonPath("$.properties['project_id']", equalTo(application.projectId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['issue_id']", equalTo(issueId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['applicant_github_id']", equalTo(antho.user().getGithubUserId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['origin']", equalTo("GITHUB")))
        );
    }

    @Test
    void should_delete_applications_when_github_issue_is_deleted() {
        // Given
        final var issueId = 1930092330L;
        final var antho = userAuthHelper.authenticateAntho();
        final var applicationId = UUID.randomUUID();

        applicationRepository.save(new ApplicationEntity(
                applicationId,
                ZonedDateTime.now(),
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                antho.user().getGithubUserId(),
                Application.Origin.GITHUB,
                issueId,
                faker.number().randomNumber(10, true),
                "My motivations",
                null
        ));

        indexingEventRepository.saveEvent(OnGithubIssueDeleted.builder()
                .id(issueId)
                .build());
        indexingEventRepository.flush();

        // When
        indexingEventsOutboxJob.run();

        // Then
        final var applications = applicationRepository.findById(applicationId);
        assertThat(applications).isEmpty();
    }

    @Test
    void should_not_be_able_to_apply_to_non_existing_project() {
        // Given
        final var githubUserId = faker.number().randomNumber(10, true);
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();

        final var projectId = UUID.fromString("77777777-4444-4444-4444-61504d34fc56");
        final var issueId = 1736474921L;
        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();

        final var request = new ProjectApplicationCreateRequest()
                .projectId(projectId)
                .issueId(issueId)
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach);

        // When
        client.post()
                .uri(getApiURI(ME_APPLICATIONS))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_return_projects_led_and_applications() {
        // Given
        final var pierre = userAuthHelper.authenticatePierre();
        final var projectAppliedTo1 = UUID.fromString("dcb3548a-977a-480e-8fb4-423d3f890c04");
        final var projectAppliedTo2 = UUID.fromString("c66b929a-664d-40b9-96c4-90d3efd32a3c");

        applicationRepository.saveAll(List.of(
                new ApplicationEntity(
                        UUID.randomUUID(),
                        ZonedDateTime.now(),
                        projectAppliedTo1,
                        pierre.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1736474921L,
                        112L,
                        "My motivations",
                        null
                ),
                new ApplicationEntity(
                        UUID.randomUUID(),
                        ZonedDateTime.now(),
                        projectAppliedTo2,
                        pierre.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1736504583L,
                        113L,
                        "My motivations",
                        null
                )
        ));

        // When
        client.get()
                .uri(ME)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "pendingApplications": [
                            {
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "motivations": "My motivations 2",
                              "problemSolvingApproach": null
                            },
                            {
                              "applicant": {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "isRegistered": true
                              },
                              "project": {
                                "id": "c66b929a-664d-40b9-96c4-90d3efd32a3c",
                                "slug": "yolo-croute",
                                "name": "Yolo croute",
                                "logoUrl": "https://i.natgeofe.com/n/8271db90-5c35-46bc-9429-588a9529e44a/raccoon_thumb_3x4.JPG"
                              },
                              "motivations": "My motivations",
                              "problemSolvingApproach": null
                            }
                          ]
                        }
                        """);
    }
}
