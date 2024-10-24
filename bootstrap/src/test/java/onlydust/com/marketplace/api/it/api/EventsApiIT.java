package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CustomIgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomIgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndexingEventRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.event.OnContributionChanged;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TagProject
public class EventsApiIT extends AbstractMarketplaceApiIT {

    private static final String API_KEY = "some-api-key";
    private final static ProjectRewardSettings REWARD_SETTINGS = new ProjectRewardSettings(true, false, true, null);
    final Long repo1 = 86943508L;
    final Long repo2 = 602953043L;
    // @formatter:off
    final List<String> repo1ContributionIds = List.of(
            "54d68518a036215aa1c004a3c7db208e4a9ba5bc7fd5372397879ef72905a8f3", // CODE_REVIEW      2017-10-06 22:33:21
            "ce48cc22cc8398b4785e405c0eae1cb1e106cee52f85b0b616d9217910cd5809", // PULL_REQUEST     2017-07-22 19:38:33
            "f9162365b1bb0b442a3756b40348b5ce9bb99a96bf2632cea6329baac5b4c0af", // PULL_REQUEST     2019-10-11 22:09:39
            "45ec2160f2e7c313829453adfb8e6d3503bfeda64eff1aebf4ea21bad3d1923f"  // PULL_REQUEST     2017-10-06 22:33:21
    );
    // @formatter:on
    final List<String> repo2ContributionIds = List.of(
            "d580dee3f8a33669633d51eccad8e4b79800e6af90bc415f4aa0d1b35e7b235e", // CODE_REVIEW      2023-02-21 17:07:22
            "f7a052432021934afeaca8898250691058aae6884c906514dea140e4ac1effef", // CODE_REVIEW      2023-02-21 17:08:12
            "420bbdd396b23d24fbe94bf6a874736b1430ecb93cc59a42642ecb4a50e79e45", // CODE_REVIEW      2023-04-24 10:36:03
            "25dfb3c26e29787d606fcccffa8fe50f6246c45623147445e1d8a44db7716162", // CODE_REVIEW      2023-04-24 10:36:03
            "0c84f988abd52d1fe7d7889860d2e049072c6bb7879758d0ebc73188f2f93da4", // ISSUE            2023-03-28 14:41:22
            "468fdb4de0a8acd91b836a2226ba4db931708f74bd372e9d60f5464c3e2f6b57", // ISSUE            2023-03-28 14:40:35
            "d6703a6b1f0b1d5a91cff05e12e8b0f088adce4f156bc73a04eb03a38786810f", // PULL_REQUEST     2023-02-21 17:07:22
            "f1f013dae1071e7b04c2edd8624a34e85a40bc70afacd1c6f62b469d4a6d97e7", // PULL_REQUEST     2023-02-21 16:43:47
            "472577e241d9c6dcbf92a512eb673ad3bba5c83bb105dd2a2fa9c64b27b83b82", // PULL_REQUEST     2023-02-21 16:47:09
            "9702804b7be03d3b0460fc145c750b36d5296298de772b5f5c89d821abf9f1dc", // PULL_REQUEST     2023-02-21 16:22:45
            "c67a731cbb8a1c4822365e90e848c4b22bc20b8115a77c4b4074152ca9b09206"  // PULL_REQUEST     2023-04-24 10:36:03
    );

    @Autowired
    IgnoredContributionsRepository ignoredContributionsRepository;
    @Autowired
    CustomIgnoredContributionsRepository customIgnoredContributionsRepository;
    @Autowired
    PostgresProjectAdapter projectStoragePort;
    @Autowired
    IndexingEventRepository indexingEventRepository;
    @Autowired
    OutboxConsumerJob indexingEventsOutboxJob;

    @BeforeEach
    void beforeEach() {
        indexerApiWireMockServer.stubFor(WireMock.post("/api/v1/events/on-repo-link-changed")
                .willReturn(WireMock.noContent()));
        databaseHelper.executeQuery("DELETE FROM indexer_exp.contributions where id != all(:ids)",
                Map.of("ids", Stream.concat(repo1ContributionIds.stream(), repo2ContributionIds.stream()).toArray(String[]::new)));
    }

    @SneakyThrows
    @AfterAll
    static void cleanUp() {
        restoreIndexerDump();
    }

    @Test
    public void should_refresh_ignored_contributions_on_contributions_change_event() {
        // Given
        final var projectId = createProject(REWARD_SETTINGS);

        customUnignoreContribution(projectId, repo1ContributionIds.get(0));

        // For now, nothing is ignored
        assertIgnored(projectId);

        Stream.of(UUID.fromString("510454f0-cf5d-3cd8-9d8a-6aa7e05dac1c"),
                        UUID.fromString("94bb8524-034c-3bd3-a072-380b2aa93d2b"),
                        UUID.fromString("dbd89cab-d3a3-385f-8e48-e67d440b1ec3"),
                        UUID.fromString("d697c288-2e17-3154-9316-845196d00a4e"))
                .map(id -> OnContributionChanged.builder().repoId(repo1).contributionUUID(id).build())
                .forEach(indexingEventRepository::saveEvent);

        // When
        indexingEventsOutboxJob.run();

        // Then
        // @formatter:off
        assertIgnored(projectId,
                // repo1
                //"54d68518a036215aa1c004a3c7db208e4a9ba5bc7fd5372397879ef72905a8f3", // CODE_REVIEW      2017-10-06 22:33:21
                "ce48cc22cc8398b4785e405c0eae1cb1e106cee52f85b0b616d9217910cd5809", // PULL_REQUEST     2017-07-22 19:38:33
                "f9162365b1bb0b442a3756b40348b5ce9bb99a96bf2632cea6329baac5b4c0af", // PULL_REQUEST     2019-10-11 22:09:39
                "45ec2160f2e7c313829453adfb8e6d3503bfeda64eff1aebf4ea21bad3d1923f"  // PULL_REQUEST     2017-10-06 22:33:21
                // repo2
                //"d580dee3f8a33669633d51eccad8e4b79800e6af90bc415f4aa0d1b35e7b235e", // CODE_REVIEW      2023-02-21 17:07:22
                //"f7a052432021934afeaca8898250691058aae6884c906514dea140e4ac1effef", // CODE_REVIEW      2023-02-21 17:08:12
                //"420bbdd396b23d24fbe94bf6a874736b1430ecb93cc59a42642ecb4a50e79e45", // CODE_REVIEW      2023-04-24 10:36:03
                //"25dfb3c26e29787d606fcccffa8fe50f6246c45623147445e1d8a44db7716162", // CODE_REVIEW      2023-04-24 10:36:03
                //"0c84f988abd52d1fe7d7889860d2e049072c6bb7879758d0ebc73188f2f93da4", // ISSUE            2023-03-28 14:41:22
                //"468fdb4de0a8acd91b836a2226ba4db931708f74bd372e9d60f5464c3e2f6b57" // ISSUE            2023-03-28 14:40:35
                //"d6703a6b1f0b1d5a91cff05e12e8b0f088adce4f156bc73a04eb03a38786810f", // PULL_REQUEST     2023-02-21 17:07:22
                //"f1f013dae1071e7b04c2edd8624a34e85a40bc70afacd1c6f62b469d4a6d97e7", // PULL_REQUEST     2023-02-21 16:43:47
                //"472577e241d9c6dcbf92a512eb673ad3bba5c83bb105dd2a2fa9c64b27b83b82", // PULL_REQUEST     2023-02-21 16:47:09
                //"9702804b7be03d3b0460fc145c750b36d5296298de772b5f5c89d821abf9f1dc", // PULL_REQUEST     2023-02-21 16:22:45
                //"c67a731cbb8a1c4822365e90e848c4b22bc20b8115a77c4b4074152ca9b09206"  // PULL_REQUEST     2023-04-24 10:36:03
        );
        // @formatter:on
    }

    @Test
    public void should_refresh_ignored_contributions_on_contributions_change_event_on_multiple_repos() {
        // Given
        final var projectId = createProject(REWARD_SETTINGS);

        customUnignoreContribution(projectId, repo1ContributionIds.get(0));
        customUnignoreContribution(projectId, repo2ContributionIds.get(0));

        // For now, nothing is ignored
        assertIgnored(projectId);

        Stream.of(UUID.fromString("510454f0-cf5d-3cd8-9d8a-6aa7e05dac1c"),
                        UUID.fromString("94bb8524-034c-3bd3-a072-380b2aa93d2b"),
                        UUID.fromString("dbd89cab-d3a3-385f-8e48-e67d440b1ec3"),
                        UUID.fromString("d697c288-2e17-3154-9316-845196d00a4e"))
                .map(id -> OnContributionChanged.builder().repoId(repo1).contributionUUID(id).build())
                .forEach(indexingEventRepository::saveEvent);

        Stream.of(UUID.fromString("d50a26e5-9622-3686-8e79-313d225b54c6"),
                        UUID.fromString("e2ed7d72-bc04-34aa-b61b-764699dcf469"),
                        UUID.fromString("dbd89cab-d3a3-385f-8e48-e67d440b1ec3"),
                        UUID.fromString("2e1f4f06-3f7b-318c-9816-8a6bb8c5281a"),
                        UUID.fromString("6f0390a2-1314-3daf-a2ba-77088b2f13de"),
                        UUID.fromString("c6b35b16-683e-3eb1-bf88-27cefc8617c0"),
                        UUID.fromString("d904b623-d15c-3a45-a6ee-a781d7aef2ec"),
                        UUID.fromString("8abac014-9c63-3364-a4e7-a0d77347664d"),
                        UUID.fromString("d697c288-2e17-3154-9316-845196d00a4e"),
                        UUID.fromString("3cbccaec-0338-3bcc-8212-d51eaf0bb3bd"),
                        UUID.fromString("7712ca5a-12ff-39a8-96c0-728ee2ed4994")
                )
                .map(id -> OnContributionChanged.builder().repoId(repo2).contributionUUID(id).build())
                .forEach(indexingEventRepository::saveEvent);

        // When
        indexingEventsOutboxJob.run();

        // Then
        // @formatter:off
        assertIgnored(projectId,
                // repo1
                //"54d68518a036215aa1c004a3c7db208e4a9ba5bc7fd5372397879ef72905a8f3", // CODE_REVIEW      2017-10-06 22:33:21
                "ce48cc22cc8398b4785e405c0eae1cb1e106cee52f85b0b616d9217910cd5809", // PULL_REQUEST     2017-07-22 19:38:33
                "f9162365b1bb0b442a3756b40348b5ce9bb99a96bf2632cea6329baac5b4c0af", // PULL_REQUEST     2019-10-11 22:09:39
                "45ec2160f2e7c313829453adfb8e6d3503bfeda64eff1aebf4ea21bad3d1923f",  // PULL_REQUEST     2017-10-06 22:33:21
                // repo2
                //"d580dee3f8a33669633d51eccad8e4b79800e6af90bc415f4aa0d1b35e7b235e", // CODE_REVIEW      2023-02-21 17:07:22
                "f7a052432021934afeaca8898250691058aae6884c906514dea140e4ac1effef", // CODE_REVIEW      2023-02-21 17:08:12
                "420bbdd396b23d24fbe94bf6a874736b1430ecb93cc59a42642ecb4a50e79e45", // CODE_REVIEW      2023-04-24 10:36:03
                "25dfb3c26e29787d606fcccffa8fe50f6246c45623147445e1d8a44db7716162", // CODE_REVIEW      2023-04-24 10:36:03
                //"0c84f988abd52d1fe7d7889860d2e049072c6bb7879758d0ebc73188f2f93da4", // ISSUE            2023-03-28 14:41:22
                //"468fdb4de0a8acd91b836a2226ba4db931708f74bd372e9d60f5464c3e2f6b57", // ISSUE            2023-03-28 14:40:35
                "d6703a6b1f0b1d5a91cff05e12e8b0f088adce4f156bc73a04eb03a38786810f", // PULL_REQUEST     2023-02-21 17:07:22
                "f1f013dae1071e7b04c2edd8624a34e85a40bc70afacd1c6f62b469d4a6d97e7", // PULL_REQUEST     2023-02-21 16:43:47
                "472577e241d9c6dcbf92a512eb673ad3bba5c83bb105dd2a2fa9c64b27b83b82", // PULL_REQUEST     2023-02-21 16:47:09
                "9702804b7be03d3b0460fc145c750b36d5296298de772b5f5c89d821abf9f1dc", // PULL_REQUEST     2023-02-21 16:22:45
                "c67a731cbb8a1c4822365e90e848c4b22bc20b8115a77c4b4074152ca9b09206"  // PULL_REQUEST     2023-04-24 10:36:03
        );
        // @formatter:on
    }

    @Test
    public void should_not_ignore_anything_is_nothing_is_to_ignore() {
        // Given
        final var projectId = createProject(new ProjectRewardSettings(false, false, false, null));

        // For now, nothing is ignored
        assertIgnored(projectId);

        indexingEventRepository.saveEvent(OnContributionChanged.builder().repoId(repo1).build());
        indexingEventRepository.saveEvent(OnContributionChanged.builder().repoId(repo2).build());

        // When
        indexingEventsOutboxJob.run();

        // Then
        assertIgnored(projectId);
    }

    private UUID createProject(ProjectRewardSettings rewardSettings) {
        final var projectId = ProjectId.random();
        final var leadId = userAuthHelper.authenticatePierre().userId();
        projectStoragePort.createProject(projectId, "name-" + projectId,
                "Name " + projectId, "a", "b", false, List.of(),
                List.of(repo1, repo2),
                leadId, List.of(), ProjectVisibility.PUBLIC, "",
                rewardSettings, List.of(), List.of(), List.of(), true, List.of());
        return projectId.value();
    }

    private void assertIgnored(UUID projectId, String... expectedIgnoredContributionIds) {
        final var ignoredContributions = ignoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(ignoredContributions).containsExactlyElementsOf(Arrays.stream(expectedIgnoredContributionIds).map(contributionId ->
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId, contributionId))
        ).toList());
    }

    private void customUnignoreContribution(UUID projectId, String contributionId) {
        customIgnoredContributionsRepository.save(CustomIgnoredContributionEntity.builder()
                .id(CustomIgnoredContributionEntity.Id.builder()
                        .projectId(projectId)
                        .contributionId(contributionId)
                        .build())
                .ignored(false)
                .build());
    }
}
