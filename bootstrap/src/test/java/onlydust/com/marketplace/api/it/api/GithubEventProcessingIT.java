package onlydust.com.marketplace.api.it.api;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndexingEventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.event.OnGithubIssueAssigned;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestCreated;
import onlydust.com.marketplace.kernel.model.event.OnPullRequestMerged;
import onlydust.com.marketplace.project.domain.model.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TagProject
public class GithubEventProcessingIT extends AbstractMarketplaceApiIT {
    @Autowired
    IndexingEventRepository indexingEventRepository;
    @Autowired
    PosthogProperties posthogProperties;
    @Autowired
    OutboxConsumerJob indexingEventsOutboxJob;
    @Autowired
    ApplicationRepository applicationRepository;

    final Faker faker = new Faker();

    private static final Long kaaperRepoId = 493591124L;
    private static final UUID kaaperProjectId = UUID.fromString("298a547f-ecb6-4ab2-8975-68f4e9bf7b39");

    @Test
    void should_publish_github_issue_assigned_event() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var createdAt = faker.date().birthday().toInstant().atZone(ZoneOffset.UTC);
        final var assignedAt = faker.date().birthday().toInstant().atZone(ZoneOffset.UTC);
        final Long issueId = 1587664960L;

        indexingEventRepository.saveEvent(OnGithubIssueAssigned.builder()
                .id(issueId)
                .repoId(kaaperRepoId)
                .assigneeId(antho.user().getGithubUserId())
                .createdAt(createdAt)
                .assignedAt(assignedAt)
                .assignedById(antho.user().getGithubUserId())
                .labels(Set.of("documentation", "good first issue"))
                .build());

        applicationRepository.save(new ApplicationEntity(
                UUID.randomUUID(),
                ZonedDateTime.now(),
                kaaperProjectId,
                antho.user().getGithubUserId(),
                Application.Origin.GITHUB,
                issueId,
                111L,
                "My motivations",
                null));

        // When
        indexingEventsOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("issue_assigned")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.timestamp", equalTo(assignedAt.toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['issue_id']", equalTo(issueId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['created_at']", equalTo(createdAt.toString())))
                .withRequestBody(matchingJsonPath("$.properties['assignee_github_id']", equalTo(antho.user().getGithubUserId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['assignee_user_id']", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['is_good_first_issue']", equalTo("true")))
        );
    }

    @Test
    void should_publish_pull_request_created_event() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var createdAt = faker.date().birthday().toInstant().atZone(ZoneOffset.UTC);
        final Long pullRequestId = faker.number().randomNumber();

        indexingEventRepository.saveEvent(OnPullRequestCreated.builder()
                .id(pullRequestId)
                .repoId(kaaperRepoId)
                .authorId(antho.user().getGithubUserId())
                .createdAt(createdAt)
                .build());

        // When
        indexingEventsOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("pull_request_created")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.timestamp", equalTo(createdAt.toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['pull_request_id']", equalTo(pullRequestId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['author_github_id']", equalTo(antho.user().getGithubUserId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['author_user_id']", equalTo(antho.user().getId().toString())))
        );
    }

    @Test
    void should_publish_pull_request_merged_event() {
        // Given
        final var antho = userAuthHelper.authenticateAntho();
        final var createdAt = faker.date().birthday().toInstant().atZone(ZoneOffset.UTC);
        final var mergedAt = createdAt.plusDays(2);
        final Long pullRequestId = faker.number().randomNumber();

        indexingEventRepository.saveEvent(OnPullRequestMerged.builder()
                .id(pullRequestId)
                .repoId(kaaperRepoId)
                .authorId(antho.user().getGithubUserId())
                .createdAt(createdAt)
                .mergedAt(mergedAt)
                .build());

        // When
        indexingEventsOutboxJob.run();

        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("pull_request_merged")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.timestamp", equalTo(mergedAt.toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent())))
                .withRequestBody(matchingJsonPath("$.properties['pull_request_id']", equalTo(pullRequestId.toString())))
                .withRequestBody(matchingJsonPath("$.properties['author_github_id']", equalTo(antho.user().getGithubUserId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['author_user_id']", equalTo(antho.user().getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['created_at']", equalTo(createdAt.toString())))
        );
    }
}
