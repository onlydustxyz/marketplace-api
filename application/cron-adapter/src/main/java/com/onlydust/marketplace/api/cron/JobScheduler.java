package com.onlydust.marketplace.api.cron;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.job.OutboxConsumerJob;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
@Profile("api")
public class JobScheduler {
    private final OutboxConsumerJob notificationOutboxJob;
    private final OutboxConsumerJob indexerOutboxJob;
    private final ProjectFacadePort projectFacadePort;

    @Scheduled(fixedDelayString = "${application.cron.notification-job-delay}")
    public void processPendingNotifications() {
        LOGGER.info("Sending pending notifications");
        notificationOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.indexer-sync-job-delay}")
    public void processPendingIndexerApiCalls() {
        LOGGER.info("Performing pending indexer API calls");
        indexerOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-ranking}")
    public void updateProjectRanking() {
        LOGGER.info("Updating projects ranking");
        projectFacadePort.updateProjectsRanking();
    }
}
