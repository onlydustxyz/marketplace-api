package com.onlydust.marketplace.api.cron;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.service.AccountingObserver;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Slf4j
@AllArgsConstructor
@Profile("jobs")
public class JobScheduler {
    private final OutboxConsumerJob indexerOutboxJob;
    private final OutboxConsumerJob trackingOutboxJob;
    private final ProjectFacadePort projectFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final UserFacadePort userFacadePort;
    private final Properties cronProperties;
    private final OutboxConsumerJob billingProfileVerificationOutboxJob;
    private final AccountingObserver accountingObserver;

    @Scheduled(fixedDelayString = "${application.cron.indexer-sync-job-delay}")
    public void processPendingIndexerApiCalls() {
        LOGGER.info("Performing pending indexer API calls");
        indexerOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.tracking-job-delay}")
    public void processPendingTrackingEvents() {
        LOGGER.info("Sending pending tracking events");
        trackingOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-ranking}")
    public void updateProjectsRanking() {
        LOGGER.info("Updating projects ranking");
        projectFacadePort.updateProjectsRanking();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-tags}")
    public void updateProjectsTags() {
        LOGGER.info("Updating projects tags");
        projectFacadePort.updateProjectsTags();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-currency-quotes}")
    public void refreshCurrencyQuotes() {
        LOGGER.info("Refreshing currency quotes");
        currencyFacadePort.refreshQuotes();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-active-user-profiles}")
    public void refreshActiveUserProfiles() {
        LOGGER.info("Refreshing active user profiles");
        userFacadePort.refreshActiveUserProfiles(ZonedDateTime.now().minusDays(cronProperties.activeUserProfilesRefreshPeriodInDays));
    }

    @Scheduled(fixedDelayString = "${application.cron.billing-profile-verification-job}")
    public void verifyBillingProfile() {
        LOGGER.debug("Verifying billing profiles");
        billingProfileVerificationOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-reward-usd-equivalents-job-delay}")
    public void refreshRewardUsdEquivalents() {
        LOGGER.info("Refreshing reward USD equivalents");
        accountingObserver.refreshRewardsUsdEquivalents();
    }

    @Data
    public static class Properties {
        Long notificationJobDelay;
        Long indexerSyncJobDelay;
        Long updateProjectsRanking;
        Long updateProjectsTags;
        Long refreshCurrencyQuotes;
        Long refreshActiveUserProfiles;
        Long activeUserProfilesRefreshPeriodInDays;
        Long trackingJobDelay;
        Long billingProfileVerificationJob;
        Long refreshRewardUsdEquivalentsJobDelay;
    }
}
