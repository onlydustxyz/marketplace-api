package com.onlydust.marketplace.api.cron;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.job.NotificationJob;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
@Profile("api")
public class JobScheduler {
    private final NotificationJob notificationJob;

    @Scheduled(fixedDelayString = "${application.cron.notification-job-delay}")
    public void processPendingNotifications() {
        LOGGER.info("Sending pending notifications");
        notificationJob.run();
    }
}
