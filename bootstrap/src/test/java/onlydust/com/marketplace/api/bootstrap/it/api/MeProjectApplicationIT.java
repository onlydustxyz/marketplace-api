package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagMe;
import onlydust.com.marketplace.api.contract.model.ApplicationRequest;
import onlydust.com.marketplace.api.contract.model.ApplicationResponse;
import onlydust.com.marketplace.api.contract.model.ApplicationUpdateRequest;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndexingEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.event.OnGithubCommentCreated;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueDeleted;
import onlydust.com.marketplace.project.domain.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class MeProjectApplicationIT extends AbstractMarketplaceApiIT {
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    IndexingEventRepository indexingEventRepository;
    @Autowired
    OutboxConsumerJob indexingEventsOutboxJob;

    @BeforeEach
    void setUp() {
        indexerApiWireMockServer.resetAll();
    }

    @Test
    void should_apply_to_project() {
        // Given
        final var user = userAuthHelper.authenticateAnthony();
        final var issueId = 1974127467L;
        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();
        final var projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

        final var request = new ApplicationRequest()
                .projectId(projectId)
                .issueId(issueId)
                .motivation(motivations)
                .problemSolvingApproach(problemSolvingApproach);

        githubWireMockServer.stubFor(post(urlEqualTo("/repository/380954304/issues/7/comments"))
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
                .expectBody(ApplicationResponse.class)
                .returnResult().getResponseBody().getId();

        final var application = applicationRepository.findById(applicationId).orElseThrow();
        assertThat(application.projectId()).isEqualTo(projectId);
        assertThat(application.applicantId()).isEqualTo(user.user().getGithubUserId());
        assertThat(application.issueId()).isEqualTo(issueId);
        assertThat(application.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(application.commentId()).isEqualTo(123456789L);
        assertThat(application.motivations()).isEqualTo(motivations);
        assertThat(application.problemSolvingApproach()).isEqualTo(problemSolvingApproach);
    }

    @Test
    void should_not_be_able_to_apply_twice() {
        // Given
        final var user = userAuthHelper.authenticateAnthony();
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

        final var request = new ApplicationRequest()
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
        final var user = userAuthHelper.authenticateAnthony();
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

        final var request = new ApplicationUpdateRequest()
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
    }

    @Test
    void should_delete_my_application() {
        // Given
        final var user = userAuthHelper.authenticateAnthony();
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
    }

    @Test
    void should_delete_an_application_as_project_lead() {
        // Given
        final var user = userAuthHelper.authenticateAnthony();
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
    }

    @Test
    void should_detect_github_application() {
        // Given
        final var commentId = faker.number().randomNumber(10, true);
        final var issueId = 1930092330L;
        final var repoId = 466482535L;
        final var antho = userAuthHelper.authenticateAnthony();
        final var commentBody = faker.lorem().sentence();

        indexingEventRepository.saveEvent(OnGithubCommentCreated.builder()
                .id(commentId)
                .issueId(issueId)
                .repoId(repoId)
                .authorId(antho.user().getGithubUserId())
                .createdAt(ZonedDateTime.now().minusSeconds(2))
                .body(commentBody)
                .build());

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
    }

    @Test
    void should_delete_applications_when_github_issue_is_deleted() {
        // Given
        final var issueId = 1930092330L;
        final var antho = userAuthHelper.authenticateAnthony();
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
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final var projectId = UUID.fromString("77777777-4444-4444-4444-61504d34fc56");
        final var issueId = 1736474921L;
        final var motivations = faker.lorem().paragraph();
        final var problemSolvingApproach = faker.lorem().paragraph();

        final var request = new ApplicationRequest()
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
                        Application.Origin.MARKETPLACE,
                        1736504583L,
                        113L,
                        "My motivations",
                        null
                )
        ));

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectsAppliedTo.length()").isEqualTo(2)
                .jsonPath("$.projectsAppliedTo[0]").isEqualTo(projectAppliedTo1.toString())
                .jsonPath("$.projectsAppliedTo[1]").isEqualTo(projectAppliedTo2.toString());
    }
}